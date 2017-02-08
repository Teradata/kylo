package com.thinkbiganalytics.nifi.feedmgr;

/*-
 * #%L
 * thinkbig-nifi-rest-client-api
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

import javax.annotation.Nonnull;

/**
 * The Input Port and Output Port in a Feed Manager Feed.
 *
 * <p>The Input Port must already exist as part of a reusable template. The Output Port will be created as a part of the NiFi feed.</p>
 */
public class InputOutputPort {

    /**
     * Input Port name from a reusable template
     */
    @Nonnull
    private final String inputPortName;

    /**
     * Output Port name for NiFi feed
     */
    @Nonnull
    private final String outputPortName;

    /**
     * Constructs a {@code InputOutputPort} with the specified input and output port names.
     *
     * @param inputPortName  the Input Port name from a reusable template
     * @param outputPortName the Output Port name for the NiFi feed
     */
    public InputOutputPort(@Nonnull final String inputPortName, @Nonnull final String outputPortName) {
        this.inputPortName = inputPortName;
        this.outputPortName = outputPortName;
    }

    /**
     * Gets the Input Port name from a reusable template.
     *
     * @return the Input Port name
     */
    @Nonnull
    public String getInputPortName() {
        return inputPortName;
    }

    /**
     * Gets the Output Port name for the NiFi feed.
     *
     * @return the Output Port name
     */
    @Nonnull
    public String getOutputPortName() {
        return outputPortName;
    }
}
