package com.thinkbiganalytics.security.rest.model;

import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

/**
 * Created by ru186002 on 18/05/2017.
 */
public class PermissionsChangeTest {

    @Test
    public void union() throws Exception {
        PermissionsChange pc = new PermissionsChange(PermissionsChange.ChangeType.ADD, "name");
        ActionGroup actions = new ActionGroup();
        Action action1 = new Action("1");
        actions.addAction(action1);
        action1.addAction(new Action("1.1"));
        Action action2 = new Action("2");
        actions.addAction(action2);
        pc.setActions(actions);

        ActionGroup otherActions = new ActionGroup();
        Action otherAction1 = new Action("1");
        otherAction1.addAction(new Action("1.2"));
        otherActions.addAction(otherAction1);

        Action otherAction2 = new Action("2");
        otherAction2.addAction(new Action("2.1"));
        otherActions.addAction(otherAction2);

        Action otherAction3 = new Action("3");
        otherAction3.addAction(new Action("3.1"));
        otherActions.addAction(otherAction3);

        pc.union(otherActions);

        ActionGroup actionSet = pc.getActionSet();
        Action a1 = actionSet.getAction("1").get();
        a1.getAction("1.1").get();
        a1.getAction("1.2").get();

        Action a2 = actionSet.getAction("2").get();
        a2.getAction("2.1").get();

        Action a3 = actionSet.getAction("3").get();
        a3.getAction("3.1").get();
    }

}