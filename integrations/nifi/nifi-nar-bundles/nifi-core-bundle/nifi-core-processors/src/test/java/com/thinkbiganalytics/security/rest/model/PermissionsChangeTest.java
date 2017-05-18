package com.thinkbiganalytics.security.rest.model;

/*-
 * #%L
 * kylo-nifi-core-processors
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.junit.Test;

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
