import org.apache.commons.lang.StringUtils
import org.json.simple.JSONObject
import org.springframework.dao.DataIntegrityViolationException

import java.text.SimpleDateFormat

class ProjectController extends BaseController {

    static allowedMethods = [ajaxSave: "POST"]
    static DATEPICKER_DATEFORMAT = new SimpleDateFormat("MM/dd/yyyy")

    String[] colors = ["#EC584C", "#386D99", "#3AB550", "#B83C8A", "#EC6E4C", "#36A968", "#B1DE48", "#2E8E8E", "#454AA4", "#ECC34C","#EC954C"]

    DatabaseService databaseService

    def beforeInterceptor = [action: this.&auth]

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
        redirect(action: "list", params: params)
    }

    def list = { ProjectFilterCommand cmd ->
        def user = findLoggedUser()
        def userInstanceList = User.withCriteria {
            eq("company", user.company)
            order(params.get('sort','name'), params.get('order','asc'))
        }
        setUpDefaultPagingParams(params)
        def projectInstanceList = listByCriteria(params, cmd, user)
        def projectInstanceTotal = countByCriteria(cmd, user)
        [projectInstanceList: projectInstanceList, projectInstanceTotal: projectInstanceTotal, project: cmd.project, startDate: cmd.startDate,
         endDate: cmd.endDate, ongoing: cmd.ongoing,active: cmd.active, userList: userInstanceList, colors: colors]
    }

    private List<Project> listByCriteria(params, cmd, user) {
        return Project.withCriteria() {
            maxResults(params.max.toInteger())
            firstResult(params.offset.toInteger())
            eq('company', user.company)
            if (cmd.project) {
                ilike('name', '%' + cmd.project + '%')
            }
            if (cmd.startDate) {
                ge('startDate', cmd.startDate)
            }
            if (cmd.endDate) {
                le('endDate', cmd.endDate)
            }
            if (cmd.ongoing) {
                def today = new Date()
                le('startDate', today)
                ge('endDate', today)
            }
            if (cmd.active)
                eq("active",true)

            ne("deleted", true)
            order("startDate", "desc")
        }
    }

    private int countByCriteria(cmd, user) {
        def criteria = Project.createCriteria()
        return criteria.get {
            projections {
                count('id')
            }
            eq('company', user.company)
            if (cmd.project) {
                ilike('name', '%' + cmd.project + '%')
            }
            if (cmd.startDate) {
                ge('startDate', cmd.startDate)
            }
            if (cmd.endDate) {
                le('endDate', cmd.endDate)
            }
            if (cmd.ongoing) {
                def today = new Date()
                le('startDate', today)
                ge('endDate', today)
            }
            if (cmd.active || cmd.active==null)
                eq("active",true)

            ne("deleted", true)
        }
    }

    def ajaxList = {
        def user = findLoggedUser()
        def projectInstanceTotal = Project.countByCompany(user.company)
        setUpDefaultPagingParams(params)
        def hideInactive = "true".equals(params.hideInactiveProjects)


        def projectInstanceList = Project.withCriteria() {
            maxResults(params.max.toInteger())
            firstResult(params.offset.toInteger())
            eq('company', user.company)
            if (hideInactive) {
                ne("active", false)
            }


            order(params.sort, params.order)
        }
        def projectInstanceListTotals = Project.withCriteria() {
            eq('company', user.company)
            if (hideInactive) {
                ne("active", false)
            }
        }
        render(template: 'list', model: [projectInstanceList: projectInstanceList, projectInstanceTotal: projectInstanceListTotals.size()])
    }

    def ajaxAssignmentsList = {
        def user = findLoggedUser()
        Project project = Project.get(params.id)
        if (!project || project.company != user.company) {
            render ''
            return
        }
        render(template: 'assignmentsList', model: [assignments: project.assignments])
    }

    def ajaxSave = {
        JSONObject jsonResponse = create(request, params)
        render jsonResponse.toString()
    }

    def ajaxProjectCreated = {
        flash.message = "project.ajax.created"
        redirect(action: "list")
    }


    def ajaxProjectDeleted = {
        flash.message = "project.ajax.deleted"
        redirect(action: "list")
    }

    def ajaxUpdate = {
        JSONObject jsonResponse = update(request, params)
        render jsonResponse.toString()
    }

    private JSONObject create(request, params) {
        JSONObject jsonResponse
        Project projectInstance = new Project(params)

        projectInstance.company = findLoggedUser().company

        if (!projectInstance.hasErrors() && projectInstance.save()) {
            jsonResponse = buildJsonOkResponse(request, buildMessageSourceResolvable('confirm'), buildMessageSourceResolvable('project.created', [projectInstance.name] as Object[]))
        } else {
            jsonResponse = buildJsonErrorResponse(request, projectInstance.errors)
        }
        return jsonResponse
    }

    private JSONObject update(request, params) {
        JSONObject jsonResponse
        def projectInstance = Project.get(params.idEdit)

        if (projectInstance && projectInstance.company.id == findLoggedUser().company.id) {
            def version = params.versionEdit.toLong()
            if (projectInstance.version > version) {
                projectInstance.errors.rejectValue("version", "project.optimistic.locking.failure", "Another user has updated this Project while you were editing")
                jsonResponse = buildJsonErrorResponse(request, projectInstance.errors)
            } else {
                Date startDate = DATEPICKER_DATEFORMAT.parse(params.startDateEdit_datePicker)
                Date endDate = DATEPICKER_DATEFORMAT.parse(params.endDateEdit_datePicker)

                projectInstance.name = params.nameEdit
                projectInstance.description = params.descriptionEdit
                projectInstance.active = params.activeEdit != null
                projectInstance.billable = params.billableEdit != null

                projectInstance.startDate = startDate
                projectInstance.endDate = endDate
                projectInstance.color = params.colorEdit
                if (StringUtils.isEmpty(projectInstance.color)){
                    projectInstance.color = getRandomColor()
                }

                if (!projectInstance.hasErrors() && projectInstance.save()) {
                    jsonResponse = buildJsonOkResponse(request, buildMessageSourceResolvable('confirm'), buildMessageSourceResolvable('project.updated', [projectInstance.name] as Object[]))
                } else {
                    jsonResponse = buildJsonErrorResponse(request, projectInstance.errors)
                }
            }
        } else {
            jsonResponse = buildJsonErrorResponse(request, buildMessageSourceResolvable('project.not.found'))
        }
        return jsonResponse
    }

    Random rand = new Random();
    String getRandomColor() {
        int randomNum = rand.nextInt(colors.length);
        return colors[randomNum]
    }

    def show = {
        def projectInstance = Project.get(params.id)
        User user = findLoggedUser()
        if (!projectInstance || projectInstance.company != user.company) {
            flash.message = "project.not.found"
            flash.args = [params.id]
            flash.defaultMessage = "Project not found with id ${params.id}"
            redirect(action: "list")
        } else {
            return [projectInstance: projectInstance, users: User.findAllByCompany(user.company), roles: Role.findAllByCompany(user.company)]
        }
    }

    def ajaxEdit = {
        def projectInstance = Project.get(params.id)
        if (!projectInstance || projectInstance.company != findLoggedUser().company) {
            flash.message = "project.not.found"
            flash.args = [params.id]
            flash.defaultMessage = "Project not found with id ${params.id}"
            redirect(action: "list")
        }
        else {
            JSONObject jsonResponse = new JSONObject()
            jsonResponse.put('okEdit', true)
            jsonResponse.put('idEdit', projectInstance.id)
            jsonResponse.put('versionEdit', projectInstance.version)
            jsonResponse.put('nameEdit', projectInstance.name)
            jsonResponse.put('descriptionEdit', projectInstance.description)
            jsonResponse.put('activeEdit', projectInstance.active)
            jsonResponse.put('billableEdit', projectInstance.billable)
            jsonResponse.put('colorEdit', projectInstance.color)

            jsonResponse.put('startDateEdit', DATEPICKER_DATEFORMAT.format(projectInstance.startDate))
            jsonResponse.put('endDateEdit', DATEPICKER_DATEFORMAT.format(projectInstance.endDate))

            render jsonResponse.toString()
        }
    }

    def edit = {
        def projectInstance = Project.get(params.id)
        if (!projectInstance || projectInstance.company != findLoggedUser().company) {
            flash.message = "project.not.found"
            flash.args = [params.id]
            flash.defaultMessage = "Project not found with id ${params.id}"
            redirect(action: "list")
        }
        else {
            render(view: "edit", model: [projectInstance: projectInstance])
            return
        }
    }

    def ajaxDelete = {
        JSONObject jsonResponse
        def projectInstance = Project.get(params.id)

        if (projectInstance) {
            try {
                projectInstance.delete()
                jsonResponse = buildJsonOkResponse(request, buildMessageSourceResolvable('confirm'), buildMessageSourceResolvable('project.deleted'))
            } catch (DataIntegrityViolationException ex) {
                jsonResponse = buildJsonOkResponse(request, buildMessageSourceResolvable('confirm'), buildMessageSourceResolvable('project.not.deleted'))
            }
        } else {
            jsonResponse = buildJsonOkResponse(request, buildMessageSourceResolvable('confirm'), buildMessageSourceResolvable('project.not.deleted'))
        }
        render jsonResponse.toString()
    }
}

class ProjectFilterCommand {
    String project
    Date startDate
    Date endDate
    Boolean ongoing
    Boolean active
}
