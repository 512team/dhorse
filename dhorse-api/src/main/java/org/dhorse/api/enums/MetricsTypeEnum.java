package org.dhorse.api.enums;

public enum MetricsTypeEnum {

	//副本指标
	REPLICA_CPU_MAX(10, "CPU", "最大值", "m"),
	REPLICA_CPU_USED(11, "CPU", "已使用", "m"),
	
	REPLICA_MEMORY_MAX(20, "内存", "最大值", "MB"),
	REPLICA_MEMORY_USED(21, "内存", "已使用", "MB"),
	
	//Jvm指标
	HEAP_MEMORY_MAX(30, "堆", "堆-最大值", "MB"),
	HEAP_MEMORY_USED(31, "堆", "堆-已使用", "MB"),
	YOUNG(32, "堆", "年轻代-已使用", "MB"),
	
	META_MEMORY_MAX(40, "元数据", "最大值", "MB"),
	META_MEMORY_USED(41, "元数据", "已使用", "MB"),
	
	GC_SIZE(50, "GC次数", "次数", "次"),
	GC_DURATION(51, "GC耗时", "耗时", "毫秒"),
	
	THREAD(60, "线程", "总数", "个数"),
	THREAD_DAEMON(61, "线程", "守护", "个数"),
	THREAD_BLOCKED(62, "线程", "阻塞", "个数"),
	THREAD_DEADLOCKED(63, "线程", "死锁", "个数"),
	;

	private Integer code;
	
	private String firstTypeName;
	
	private String secondeTypeName;
	
	private String unit;

	private MetricsTypeEnum(Integer code, String firstTypeName, String secondeTypeName, String unit) {
		this.code = code;
		this.firstTypeName = firstTypeName;
		this.secondeTypeName = secondeTypeName;
		this.unit = unit;
	}

	public static MetricsTypeEnum getByCode(Integer code) {
		if(code == null) {
			return null;
		}
		MetricsTypeEnum[] values = values();
		for(MetricsTypeEnum v : values) {
			if(code.equals(v.getCode())) {
				return v;
			}
		}
		return null;
	}
	
	public Integer getCode() {
		return code;
	}

	public String getFirstTypeName() {
		return firstTypeName;
	}

	public String getSecondeTypeName() {
		return secondeTypeName;
	}

	public String getUnit() {
		return unit;
	}
}
