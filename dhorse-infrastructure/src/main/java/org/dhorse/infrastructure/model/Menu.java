package org.dhorse.infrastructure.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 菜单模型
 */
public class Menu implements Serializable {

	private static final long serialVersionUID = 1L;

	private String title;

	private String href;

	private String icon;

	private String target;

	private List<Menu> child = new ArrayList<>();

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public List<Menu> getChild() {
		return child;
	}

	public void addChild(Menu menu) {
		this.child.add(menu);
	}

}