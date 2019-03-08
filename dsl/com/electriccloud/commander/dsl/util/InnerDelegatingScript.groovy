package com.electriccloud.commander.dsl.util

/**
 * Script base class for nested DSL scripts evaluated inline.
 */
abstract class InnerDelegatingScript extends DelegatingScript {

  def bindingVariables

  public void setBinding(Binding binding) {
    super.setBinding(binding)
      bindingVariables = binding.variables
  }

  // NMB-27865: When looking up a property, give precedence
  // to the bind variables passed in to the nested DSL script
  // before delegating up the chain.
  public Object getProperty(String property) {
      if (!property.equals('bindingVariables') && bindingVariables != null && bindingVariables.containsKey(property)) {
          return bindingVariables.get(property)
      } else {
          return super.getProperty(property)
      }
  }

}
