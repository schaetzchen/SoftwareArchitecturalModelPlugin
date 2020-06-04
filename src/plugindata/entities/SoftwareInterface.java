package plugindata.entities;

import java.util.Set;

public class SoftwareInterface {

    private String name;
    private SoftwareComponent belongingComponent;
    private Set<String> methodNames;

    public SoftwareInterface(String name, SoftwareComponent belongingComponent, Set<String> methodNames) {
        this.name = name;
        this.belongingComponent = belongingComponent;
        this.methodNames = methodNames;
        belongingComponent.addInterface(this);
    }

    public SoftwareInterface(SoftwareComponent belongingComponent, Set<String> methodNames) {
        this.name = belongingComponent.getName() + "_Interface" + belongingComponent.getInterfacesCount();
        this.belongingComponent = belongingComponent;
        this.methodNames = methodNames;
        belongingComponent.addInterface(this);
    }

    public String getName() {
        return name;
    }

    public SoftwareComponent getBelongingComponent() {
        return belongingComponent;
    }

    public Set<String> getMethodNames() {
        return methodNames;
    }

    public boolean containsMethod(String method)  {

        for (String s : methodNames)
            if (s.equals(method))
                return true;

        return false;
    }
}
