package org.dhorse.infrastructure.strategy.cluster.k8s;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dhorse.api.response.model.EnvPrometheus;
import org.dhorse.infrastructure.model.JsonPatch;
import org.dhorse.infrastructure.utils.DeploymentContext;
import org.springframework.util.CollectionUtils;

public class PrometheusHelper {
	
	public static Map<String, String> addPrometheus(String kind, DeploymentContext context) {
		Map<String, String> annotations = new HashMap<>();
		annotations.put("prometheus.io/scrape", "false");
		EnvPrometheus ep = context.getEnvPrometheus();
		if(ep == null) {
			return annotations;
		}
		if(!kind.equals(ep.getKind())){
			return annotations;
		}
		annotations.put("prometheus.io/scrape", ep.getScrape());
		annotations.put("prometheus.io/port", ep.getPort());
		annotations.put("prometheus.io/path", ep.getPath());
		return annotations;
	}
	
	public static List<JsonPatch> updatePrometheus(String kind, Map<String, String> existedAnnotations, DeploymentContext context) {
		List<JsonPatch> paths = new ArrayList<>();
		EnvPrometheus ep = context.getEnvPrometheus();
		if(ep == null && !CollectionUtils.isEmpty(existedAnnotations)
				&& "true".equals(existedAnnotations.get("prometheus.io/scrape"))) {
			JsonPatch scrape = new JsonPatch();
			scrape.setOp("replace");
			scrape.setPath("/metadata/annotations/prometheus.io~1scrape");
			scrape.setValue("false");
			paths.add(scrape);
			return paths;
		}
		
		if(ep != null && !kind.equals(ep.getKind())){
			JsonPatch scrape = new JsonPatch();
			scrape.setOp("replace");
			scrape.setPath("/metadata/annotations/prometheus.io~1scrape");
			scrape.setValue("false");
			paths.add(scrape);
			return paths;
		}
		
		if(ep != null && kind.equals(ep.getKind()) && (existedAnnotations == null 
				|| !ep.getScrape().equals(existedAnnotations.get("prometheus.io/scrape"))
				|| !ep.getPort().equals(existedAnnotations.get("prometheus.io/port"))
				|| !ep.getPath().equals(existedAnnotations.get("prometheus.io/path")))) {
			JsonPatch scrape = new JsonPatch();
			scrape.setOp("replace");
			scrape.setPath("/metadata/annotations/prometheus.io~1scrape");
			scrape.setValue(ep.getScrape());
			
			JsonPatch port = new JsonPatch();
			port.setOp("replace");
			port.setPath("/metadata/annotations/prometheus.io~1port");
			port.setValue(ep.getPort());
			
			JsonPatch path = new JsonPatch();
			path.setOp("replace");
			path.setPath("/metadata/annotations/prometheus.io~1path");
			path.setValue(ep.getPath());
			
			paths.add(scrape);
			paths.add(port);
			paths.add(path);
			return paths;
		}
		
		return paths;
	}
}