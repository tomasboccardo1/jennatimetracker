<div class="list">
  <table>
    <thead>
    <tr>
      <th/>
      <g:sortableColumn property="name" title="Name" titleKey="project.name"/>
      <g:sortableColumn property="startDate" title="Start Date" titleKey="project.startDate"/>
      <g:sortableColumn property="endDate" title="End Date" titleKey="project.endDate"/>
    </tr>
    </thead>
    <tbody>
    <g:each in="${projectInstanceList}" status="i" var="projectInstance">
      <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
        <td>
          <a href="javascript:edit(${projectInstance.id});"><img src="${resource(dir: 'images', file: 'edit_16x16.png')}" border="0"/></a>
          <a href="${createLink(action: 'show', id: projectInstance.id)}"><img src="${resource(dir: 'images', file: 'lens_16x16.png')}" border="0"/></a>
        </td>
        <td>${fieldValue(bean: projectInstance, field: "name")}<span style="background-color: ${fieldValue(bean: projectInstance, field: "color")};   display: inline-block;
        width: 15px;
        height: 15px;
        border-radius: 4px;
        margin-left: 10px;"/> </td>
        <td><g:formatDate date="${projectInstance.startDate}" type="date" style="short"/></td>
        <td><g:formatDate date="${projectInstance.endDate}" type="date" style="short"/></td>
      </tr>
    </g:each>
    </tbody>
  </table>
</div>
<div class="paginateButtons">
  <g:paginate total="${projectInstanceTotal}"/>
</div>
