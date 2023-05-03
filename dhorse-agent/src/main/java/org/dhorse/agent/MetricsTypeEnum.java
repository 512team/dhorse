package org.dhorse.agent;

/**
 * 指标类型
 * 
 * @author 无双
 * 2023-05-02
 */
public class MetricsTypeEnum {

	public static enum FirstType {

		REPLICA_MEMORY(1, "副本内存"),
		REPLICA_CPU(2, "副本CPU"),
		HEAP_MEMORY(3, "堆内存"),
		NON_HEAP_MEMORY(4, "非堆内存"),
		META_MEMORY(5, "元数据内存"),
		THREAD(6, "线程"),
		GC(7, "GC");

		private Integer code;

		private String value;

		private FirstType(Integer code, String value) {
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

	public static class SecondType {
		
		public static enum Memory {

			USED(101, "已使用"),
			COMMITTED(102, "已申请"),
			MAX(103, "最大");

			private Integer code;

			private String value;

			private Memory(Integer code, String value) {
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

		public static enum Thread {

			DEFAULT(104, "总数"),
			DAEMON(105, "守护线程"),
			BLOCKED(106, "阻塞线程"),
			DEADLOCKED(107, "死锁线程");

			private Integer code;

			private String value;

			private Thread(Integer code, String value) {
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

		public static enum GC {

			SIZE(109, "次数"),
			TIME(109, "耗时");

			private Integer code;

			private String value;

			private GC(Integer code, String value) {
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
	}
}
