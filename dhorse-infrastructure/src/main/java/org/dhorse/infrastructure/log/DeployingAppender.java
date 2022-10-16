package org.dhorse.infrastructure.log;


import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.status.ErrorStatus;

/**
 *  部署日志处理器。
 * @author Dahai
 *  2021-11-16 17:24:46
 */
public class DeployingAppender<E> extends AppenderBase<E> {

    // 注意：这边的属性名一定要与logback.xml中的标签名一致
    private DeployingPolicy policy;
    // 注意：这边的属性名一定要与logback.xml中的标签名一致
    private Layout<E> layout;

    public void start() {
        super.start();
        if (this.layout == null) {
            this.addStatus(new ErrorStatus("No layout set for the appender named \"" + this.name + "\".", this));
        }
    }

    @Override
    protected void append(E eventObject) {
        if (this.policy != null) {
            this.policy.handler(this.layout.doLayout(eventObject));
        }
    }

    public DeployingPolicy getPolicy() {
        return policy;
    }

    public void setPolicy(DeployingPolicy myPolicy) {
        this.policy = myPolicy;
    }

    public Layout<E> getLayout() {
        return layout;
    }

    public void setLayout(Layout<E> layout) {
        this.layout = layout;
    }
}