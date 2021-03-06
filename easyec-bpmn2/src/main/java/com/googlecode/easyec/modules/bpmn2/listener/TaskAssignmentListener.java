package com.googlecode.easyec.modules.bpmn2.listener;

import com.googlecode.easyec.modules.bpmn2.assignment.Assignee;
import com.googlecode.easyec.modules.bpmn2.domain.ExtraTaskObject;
import com.googlecode.easyec.modules.bpmn2.domain.ProcessObject;
import com.googlecode.easyec.modules.bpmn2.domain.impl.ExtraTaskObjectImpl;
import com.googlecode.easyec.modules.bpmn2.service.ProcessPersistentException;
import com.googlecode.easyec.modules.bpmn2.service.QueryProcessService;
import com.googlecode.easyec.modules.bpmn2.service.UserTaskService;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.googlecode.easyec.modules.bpmn2.utils.ProcessConstant.PROCESS_ENTITY_ID;
import static com.googlecode.easyec.spirit.web.utils.SpringContextUtils.getBean;
import static org.apache.commons.lang.StringUtils.isBlank;

/**
 * 任务分配的监听类
 *
 * @author JunJie
 */
public abstract class TaskAssignmentListener implements TaskListener {

    private static final long serialVersionUID = 6986390784087974029L;
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void notify(DelegateTask delegateTask) {
        ProcessObject process = getBean(QueryProcessService.class).getProcess(
            (Long) delegateTask.getVariable(PROCESS_ENTITY_ID)
        );

        if (process == null) {
            logger.debug("No ProcessObject was found.");

            return;
        }

        logger.debug("ProcessObject uid: [{}].", process.getUidPk());

        if (isBlank(process.getProcessInstanceId())) {
            logger.debug("Current Process object hasn't instance id. So it should copy from current task.");

            process.setProcessInstanceId(delegateTask.getProcessInstanceId());
        }

        // 同步当前任务的实例ID给流程对象
        process.setProcessDefinitionId(delegateTask.getProcessDefinitionId());

        // 获取当前任务的处理人
        Assignee assignee = getAssignee(delegateTask, process);

        // 创建任务的扩展信息
        ExtraTaskObject obj = new ExtraTaskObjectImpl();
        obj.setProcessInstanceId(delegateTask.getProcessInstanceId());
        obj.setCreateTime(delegateTask.getCreateTime());
        obj.setTaskId(delegateTask.getId());

        if (assignee != null) {
            obj.setAssignee(assignee.getUserId());
            obj.setDelegatedUser(assignee.getDelegatedUserId());

            // 持久化处理人
            delegateTask.setAssignee(assignee.getUserId());
        }

        try {
            getBean(UserTaskService.class).createExtraTask(obj);
        } catch (ProcessPersistentException e) {
            throw new ActivitiIllegalArgumentException("Cannot create extra-task.", e);
        }
    }

    /**
     * 通过给定的流程实体对象，
     * 查找当前任务节点的处理人。
     *
     * @param task    委托任务对象
     * @param process 流程实体对象
     * @return 任务负责人对象
     */
    abstract protected Assignee getAssignee(DelegateTask task, ProcessObject process);
}
