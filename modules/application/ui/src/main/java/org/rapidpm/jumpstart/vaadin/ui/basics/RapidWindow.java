package org.rapidpm.jumpstart.vaadin.ui.basics;

import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import org.rapidpm.jumpstart.vaadin.logic.properties.PropertyService;

import javax.inject.Inject;

/**
 * Created with IntelliJ IDEA.
 * User: marco
 * Date: 23.11.12
 * Time: 13:04
 * This is part of the RapidPM - www.rapidpm.org project. please contact chef@sven-ruppert.de
 */
public class RapidWindow extends Window {

  public @Inject PropertyService propertyService;

  private final VerticalLayout contentLayout = new VerticalLayout();

  public RapidWindow() {
    contentLayout.setSpacing(true);
    contentLayout.setMargin(true);
    setContent(contentLayout);
    setModal(true);
  }

  public void addComponent(final Component component) {
    contentLayout.addComponent(component);
  }

  public void removeAllComponents() {
    setContent(null);
  }

  public VerticalLayout getContentLayout() {
    return contentLayout;
  }
}
