package com.electriccloud.commander.dsl.util

/**
 * Script base class for nested DSL scripts evaluated inline.
 */
abstract class InnerDelegatingScript extends DelegatingScript {

  def bindingVariables

  @Override
  public void setBinding(Binding binding) {
    super.setBinding(binding)

    bindingVariables = binding.variables
  }

  // NMB-27865: When looking up a property, give precedence
  // to the bind variables passed in to the nested DSL script
  // before delegating up the chain.
  @Override
  public Object getProperty(String property) {
      if (!property.equals('bindingVariables') && bindingVariables != null && bindingVariables.containsKey(property)) {
          return bindingVariables.get(property)
      } else {
          return super.getProperty(property)
      }
  }

  @Override
  public Object invokeMethod(String name, Object args) {
    // BEE-18910: pass property sheet id required for property and property sheet evaluation
    Map<String, Object> propertyArgs = getBindingVariables()

    if ("property".equals(name) && args && args.length > 0
            && propertyArgs.containsKey("objectId")
            && propertyArgs.containsKey("propertyType")) {

        if (args[0] instanceof Map && propertyArgs.get("propertyType") == 'string') {

            args[0].putAll(propertyArgs)
        }
        else {

            Object[] newArgs = new Object[args.length + 1]
            newArgs[0]       = propertyArgs

            for (int i = 0; i < args.length; i++) {
                newArgs[i + 1] = args[i]
            }

            args = newArgs
        }
    }

    return super.invokeMethod(name, args)
  }
}
