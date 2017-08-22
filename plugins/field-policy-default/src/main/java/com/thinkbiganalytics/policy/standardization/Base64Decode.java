package com.thinkbiganalytics.policy.standardization;

/*-
 * #%L
 * thinkbig-field-policy-default
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

import com.thinkbiganalytics.policy.PolicyProperty;
import com.thinkbiganalytics.policy.PolicyPropertyRef;
import com.thinkbiganalytics.policy.PolicyPropertyTypes;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;


@Standardizer(
    name = "Base64 Decode",
    description = "Base64 decode a string or a byte[].  Strings are evaluated using the UTF-8 charset")
public class Base64Decode implements StandardizationPolicy {

    private static final Logger log = LoggerFactory.getLogger(Base64Decode.class);

    public enum Base64Output {
        BINARY, STRING
    }

    @PolicyProperty(name = "Output", hint = "Choose to decode and return as a binary(byte[]) or as a string", type = PolicyPropertyTypes.PROPERTY_TYPE.select,
                    selectableValues = {"BINARY", "STRING"}, required = true)
    private Base64Decode.Base64Output base64Output = Base64Decode.Base64Output.BINARY;

    public Base64Decode(@PolicyPropertyRef(name = "Output") Base64Decode.Base64Output output) {
        super();
        this.base64Output = output;
    }


    @Override
    public String convertValue(String value) {
        throw new UnsupportedOperationException("Conversion to base64 encoded string back to a byte array");
    }

    @Override
    public Boolean accepts(Object value) {
        return (value instanceof byte[]) || (value instanceof String);
    }

    @Override
    public Object convertRawValue(Object value) {
        try {
            byte[] val = null;
            if (value instanceof byte[]) {
                val = (byte[]) value;
            } else {
                val = ((String) value).getBytes("UTF-8");
            }
            if (Base64Output.BINARY.equals(base64Output)) {
                byte[] decoded = Base64.decodeBase64(val);
                return decoded;
            } else if (Base64Output.STRING.equals(base64Output)) {
                byte[] v = Base64.decodeBase64(val);
                return new String(v, "UTF-8");
            }

        } catch (UnsupportedEncodingException e) {
            return null;
        }
        return null;
    }
}
