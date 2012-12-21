import chat.Answer
import org.springframework.context.MessageSource

/**
 * Created with IntelliJ IDEA.
 * User: federico
 * Date: 12/21/12
 * Time: 10:48 AM
 * To change this template use File | Settings | File Templates.
 */
class AskForHumourRequestHandler extends RequestHandler {

    def grailsApplication

    def accepts(Conversation _conversation) {
        boolean accepted = _conversation.context.askForHumour
        return accepted
    }

    @Override
    def doHandle(Conversation _conversation, ChatService _chatService) {

        final User user;
        user = _conversation.actualRequest.user
        String response = _conversation.actualRequest.message
        Answer answer = SalutationStep3RequestHandler.answerEvaluation(response);
        GregorianCalendar calendar = GregorianCalendar.getInstance();

        if (_conversation.isAffirmative()){
            _conversation.responses << Response.build('negativeHumourResponseHandler')
            _conversation.context.saveResponse=true;
            _conversation.context.askForHumour = true

        } else if (_conversation.isNegative() || _conversation.context.saveResponse ){

            if (_conversation.context.saveResponse){
                // If this is executed, the response matched a valid option
                UserMood uMood = new UserMood()
                uMood.user = user
                uMood.date = new java.sql.Date(calendar.getTimeInMillis());
                uMood.value = _conversation.context.moodValue
                uMood.status = ""
                uMood.company = user.company
                uMood.reason = response;
                uMood.save(flush: true)
            }

            // List user assignments so that workflow will change in case user doesn't have any.
            def assignments = user.listActiveAssignments()
            if (assignments) {
                List<Response> responses = SalutationStep3RequestHandler.getAnswerFor(answer)
                _conversation.responses = responses
                _conversation.context.clear()
                _conversation.context.salutateStep4 = true
            } else {
                _conversation.responses << Response.build('SalutationRequestHandlerStep3NoAssignments')
                _conversation.context.clear()
                _conversation.context.askForKnowledge = true
                KnowledgeStep1RequestHandler handler = new KnowledgeStep1RequestHandler()
                handler.handle(_conversation, _chatService)
            }

        } else {
            _conversation.responses << Response.build('negativeHumourUnknowResponseHandler')
            _conversation.context.askForHumour = true
            return
        }

    }
}
