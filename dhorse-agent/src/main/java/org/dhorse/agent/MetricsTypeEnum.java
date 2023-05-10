package org.dhorse.agent;

/**
 * 指标类型
 * 
 * @author 无双 2023-05-02
 */
public enum MetricsTypeEnum {

	//副本指标
	REPLICA_MEMORY_USED(1, "副本内存-已使用"),
	REPLICA_MEMORY_MAX(2, "副本内存-最大"),
	REPLICA_CPU_USED(3, "副本CPU-已使用"),
	REPLICA_CPU_MAX(4, "副本CPU-最大"),
	
	//Jvm指标
	HEAP_MEMORY_USED(5, "堆内存-已使用"),
	HEAP_MEMORY_MAX(6, "堆内存-最大"),
	YOUNG(7, "年轻代-已使用"),
	META_MEMORY_USED(8, "元数据-已使用"),
	GC_SIZE(9, "GC-次数"),
	GC_DURATION(10, "GC-耗时"),
	THREAD(11, "线程-总数"),
	THREAD_DAEMON(12, "线程-守护数"),
	THREAD_BLOCKED(13, "线程-阻塞数"),
	THREAD_DEADLOCKED(14, "线程-死锁数"),
	;

	private Integer code;

	private String value;

	private MetricsTypeEnum(Integer code, String value) {
		this.code = code;
		this.value = value;
	}

	public Integer getCode() {
		return code;
	}

	public String getValue() {
		return value;
	}
}
