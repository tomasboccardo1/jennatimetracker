import groovy.time.TimeCategory
import org.apache.commons.lang.StringUtils
import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.springframework.dao.DataIntegrityViolationException

import java.text.SimpleDateFormat

class EffortController extends BaseController {

    // the delete, save and update actions only accept POST requests
    static allowedMethods = [save: "POST", update: "POST", delete: "POST", ajaxGetAssignments: "GET"]

    static FULLCALENDAR_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'00:00:00")

    def beforeInterceptor = [action: this.&auth]
    def grailsApplication

    def auth() {
        try {
            findLoggedUser()
            return true
        } catch (Exception e) {
            redirect(controller: 'login', action: 'auth')
            return false
        }
    }


    def index = {
        redirect(action: 'myList', params: params)
    }

    def list = {
        def user = findLoggedUser()
        User reqUser
        if (params['user.id']) {
            reqUser = User.get(params['user.id'].toInteger())
        }
        if (!reqUser || reqUser.company != user.company) {
            reqUser = user
        }
        [users: User.findAllByCompany(findLoggedUser().company, [sort: "name", order: "asc"]), userId: reqUser.id]
    }

    def calendar = {
        Date fromDate = new Date(params.start.toLong()).clearTime()
        Date toDate = new Date(params.end.toLong()).clearTime()
        def user = findLoggedUser()
        User reqUser
        if (params.userId) {
            reqUser = User.get(params.userId.toInteger())
        }
        if (!reqUser || reqUser.company != user.company) {
            reqUser = user
        }
        def effortInstanceList = Effort.withCriteria() {
            eq('user', reqUser)
            ge('date', fromDate)
            lt('date', toDate)
            gt('timeSpent', 0D)
            ne('deleted', true)
        }
        JSONArray jsonResponse = new JSONArray()
        effortInstanceList.each { Effort effort ->
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("id", effort.id);
            jsonObj.put("color", getColorForProject(effort));
            jsonObj.put("title", getTitleForEffort(effort));
            jsonObj.put("comment", effort.comment ?: '');
            jsonObj.put("currentDate", g.formatDate(date: effort.date, type: 'date', style: 'short'));
            jsonObj.put("start", FULLCALENDAR_DATE_FORMATTER.format(effort.date));
            jsonObj.put("assignmentList", getAssignmentCalendarFormat(effort.assignment));
            jsonObj.put("timeSpent", effort.timeSpent);
            jsonResponse.push(jsonObj)
        }
        render jsonResponse.toString()
    }

    /**
     * Get the project color defined in model Project.color
     * if not present, a default one is given
     * @param effort
     * @return
     */
    Object getColorForProject(Effort effort) {
        String color = null;
        if (effort.assignment != null && effort.assignment.project != null)
            color =  effort.assignment.project.color

        if (StringUtils.isEmpty(color)){
            color = grailsApplication.config['jenna']['defaultProjectcolor']
        }
        return color;
    }
    def myList = {
    }

    private String getTitleForEffort(Effort effort) {
        String title = "$effort.timeSpent hs: "
        if (effort.assignment != null)
            title += "(${effort.assignment?.project?.name} - ${effort.assignment?.role?.name})"
        else if (effort.tags?.size() > 0) {
            title += "${effort.tags.join(', ')}"
        } else {
            title += "" + g.message(code: "effort.assignment.not.specified")
        }

        return title
    }

    def myCalendar = {
        Date fromDate = new Date(params.start.toLong()).clearTime()
        Date toDate = new Date(params.end.toLong()).clearTime()
        def user = findLoggedUser()
        def effortInstanceList = Effort.withCriteria() {
            eq('user', user)
            ge('date', fromDate)
            lt('date', toDate)
            gt('timeSpent', 0D)
            ne("deleted", true)
        }
        JSONArray jsonResponse = new JSONArray()
        effortInstanceList.each { Effort effort ->
            jsonResponse.add(
                    [id            : effort.id,
                     color: getColorForProject(effort),
                     title: getTitleForEffort(effort),
                     comment       : effort.comment ?: '',
                     currentDate   : g.formatDate(date: effort.date, type: 'date', style: 'short'),
                     start         : FULLCALENDAR_DATE_FORMATTER.format(effort.date),
                     assignmentList: getAssignmentCalendarFormat(effort.assignment),
                     timeSpent     : effort.timeSpent]
            )
        }
        render jsonResponse.toString()
    }

    private String getAssignmentCalendarFormat(Assignment ass) {
        String assignmentCalendarFormat = ""
        String project = g.message(code: "assignment.project")
        String role = g.message(code: "assignment.permission")

        if (ass == null) {
            assignmentCalendarFormat = g.message(code: "assignment.not.specified")
        } else {
            assignmentCalendarFormat += project + ": "
            assignmentCalendarFormat += ass.project
            assignmentCalendarFormat += " - "
            assignmentCalendarFormat += role + ": "
            assignmentCalendarFormat += ass.role?.name + "."
        }
        return assignmentCalendarFormat

    }

    // TODO: Not used anymore?
    def create = {
        def effortInstance = new Effort()
        effortInstance.properties = params
        return [effortInstance: effortInstance]
    }

    // TODO: Not used anymore?
    def save = {
        def effortInstance = new Effort(params)
        if (!effortInstance.hasErrors() && effortInstance.save()) {
            flash.message = "effort.created"
            flash.args = [effortInstance.id]
            flash.defaultMessage = "Effort ${effortInstance.id} created"
            redirect(action: "show", id: effortInstance.id)
        } else {
            render(view: "create", model: [effortInstance: effortInstance])
        }
    }

    def ajaxSave = {
        JSONObject jsonResponse = params.id ? update(request, params) : create(request, params)
        render jsonResponse.toString()
    }

    public JSONObject create(request, params) {
        JSONObject jsonResponse

        Effort effortInstance = new Effort(params)

        effortInstance.user = findLoggedUser()

        Assignment assignment = Assignment.get(params.assignmentId)
        effortInstance.assignment = assignment

        if (!effortInstance.hasErrors() && effortInstance.save()) {
            jsonResponse = buildJsonOkResponse(request, buildMessageSourceResolvable('confirm'), buildMessageSourceResolvable('effort.created'))
        } else {
            jsonResponse = buildJsonErrorResponse(request, effortInstance.errors)
        }
        return jsonResponse
    }

    public JSONObject update(request, params) {
        JSONObject jsonResponse
        def effortInstance = Effort.get(params.id)
        if (effortInstance && effortInstance.user.id == findLoggedUser().id) {
            def version = params.version.toLong()
            if (effortInstance.version > version) {
                effortInstance.errors.rejectValue("version", "effort.optimistic.locking.failure", "Another user has updated this Effort while you were editing")
                jsonResponse = buildJsonErrorResponse(request, effortInstance.errors)
            } else {
                bindData(effortInstance, params, [exclude: 'date'])

                Assignment assignment = Assignment.get(params.assignmentId)
                effortInstance.assignment = assignment

                if (!effortInstance.hasErrors() && effortInstance.save()) {
                    jsonResponse = buildJsonOkResponse(request, buildMessageSourceResolvable('confirm'), buildMessageSourceResolvable('effort.updated'))
                } else {
                    jsonResponse = buildJsonErrorResponse(request, effortInstance.errors)
                }
            }
        } else {
            jsonResponse = buildJsonErrorResponse(request, buildMessageSourceResolvable('effort.not.found'))
        }
        return jsonResponse
    }

    // TODO: Not used anymore?
    def show = {
        def effortInstance = Effort.get(params.id)
        if (!effortInstance) {
            flash.message = "effort.not.found"
            flash.args = [params.id]
            flash.defaultMessage = "Effort not found with id ${params.id}"
            redirect(action: "list")
        } else {
            return [effortInstance: effortInstance]
        }
    }

    // TODO: Not used anymore?
    def edit = {
        def effortInstance = Effort.get(params.id)
        if (!effortInstance) {
            flash.message = "effort.not.found"
            flash.args = [params.id]
            flash.defaultMessage = "Effort not found with id ${params.id}"
            redirect(action: "list")
        } else {
            return [effortInstance: effortInstance]
        }
    }

    def ajaxEdit = {
        def effortInstance = Effort.get(params.id)
        if (!effortInstance) {
            flash.message = "effort.not.found"
            flash.args = [params.id]
            flash.defaultMessage = "Project not found with id ${params.id}"
            redirect(action: "list")
        } else {
            JSONObject jsonResponse = new JSONObject()
            jsonResponse.put('ok', true)
            jsonResponse.put('id', effortInstance.id)
            jsonResponse.put('version', effortInstance.version)
            jsonResponse.put('date_day', effortInstance.date.date)
            jsonResponse.put('date_month', effortInstance.date.month + 1)
            jsonResponse.put('date_year', effortInstance.date.year + 1900)
            // FIXME: As the number is going to be translated using the browser's locale when the user sends the form, we must transform it.
            // However, this is not consistent with how we show numbers in the rest of the app.
            // Handling i18n for number separators is hard, but should be consistent :(
            jsonResponse.put('timeSpent', formatNumber(number: effortInstance.timeSpent, format: '##0.#'))
            jsonResponse.put('assignmentId', effortInstance.assignment?.id)
            jsonResponse.put('comment', effortInstance.comment)

            List assignmentsList = findAssignmentsForUserAndDate(effortInstance.user, effortInstance.date)
            String assignments = convertAssignmentsToSelectComboContent(assignmentsList, effortInstance.assignment?.id)
            jsonResponse.put('assignmentList', assignments)

            render jsonResponse.toString()
        }
    }

    def update = {
        def effortInstance = Effort.get(params.id)
        if (effortInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (effortInstance.version > version) {

                    effortInstance.errors.rejectValue("version", "effort.optimistic.locking.failure", "Another user has updated this Effort while you were editing")
                    render(view: "edit", model: [effortInstance: effortInstance])
                    return
                }
            }
            effortInstance.properties = params
            if (!effortInstance.hasErrors() && effortInstance.save()) {
                flash.message = "effort.updated"
                flash.args = [params.id]
                flash.defaultMessage = "Effort ${params.id} updated"
                redirect(action: "show", id: effortInstance.id)
            } else {
                render(view: "edit", model: [effortInstance: effortInstance])
            }
        } else {
            flash.message = "effort.not.found"
            flash.args = [params.id]
            flash.defaultMessage = "Effort not found with id ${params.id}"
            redirect(action: "edit", id: params.id)
        }
    }

    def ajaxDelete = {
        JSONObject jsonResponse
        def effortInstance = Effort.get(params.id)
        if (effortInstance && effortInstance.user.id == findLoggedUser().id) {
            try {
                effortInstance.delete()
                jsonResponse = buildJsonOkResponse(request, buildMessageSourceResolvable('confirm'), buildMessageSourceResolvable('effort.deleted'))
            } catch (DataIntegrityViolationException ex) {
                jsonResponse = buildJsonOkResponse(request, buildMessageSourceResolvable('confirm'), buildMessageSourceResolvable('effort.not.deleted'))
            }
        } else {
            jsonResponse = buildJsonOkResponse(request, buildMessageSourceResolvable('confirm'), buildMessageSourceResolvable('effort.not.deleted'))
        }
        render jsonResponse.toString()
    }


    private static dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    def ajaxGetAssignments = {
        JSONObject jsonResponse = new JSONObject()
        Date date = dateFormat.parse("$params.year-$params.month-$params.day")

        User user = findLoggedUser()

        List assignmentsList = findAssignmentsForUserAndDate(user, date)

        if (!assignmentsList.isEmpty()) {
            String assignments = convertAssignmentsToSelectComboContent(assignmentsList, null)

            jsonResponse.put('assignmentList', assignments)
            jsonResponse.put('ok', true)
        }

        render jsonResponse.toString()
    }

    private String convertAssignmentsToSelectComboContent(ArrayList assignmentList, Long selected) {

        String assignments = ""
        String openOption = "<option value=\""
        String openOptionClosure = "\">"
        String openOptionClosureSelected = "\" selected=\"selected\">"
        String optionClosure = "</option>"

        assignmentList.each { Assignment ass ->
            assignments += openOption + ass.id
            if (selected != null && ass.id == selected)
                assignments += openOptionClosureSelected
            else
                assignments += openOptionClosure

            assignments += ass.toString() + optionClosure
        }

        return assignments

    }

    /**
     * Returns a list of assignment for the user, that is active for the given date
     */
    private List findAssignmentsForUserAndDate(final User user, final Date date) {
        use(TimeCategory) {
            final Date beginning = date.clone().clearTime() + 1.day
            final Date end = date.clone().clearTime() - 1.millisecond

            return Assignment.withCriteria {
                eq("user", user)
                lt("startDate", beginning)
                gt("endDate", end)
                ne("deleted", true)
                eq("active", true)
            }
        }

    }
}
