begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.rest.section
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|rest
operator|.
name|section
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|logging
operator|.
name|ESLogger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|logging
operator|.
name|Loggers
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|equalTo
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|instanceOf
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertThat
import|;
end_import

begin_comment
comment|/**  * Represents a length assert section:  *  *   - length:   { hits.hits: 1  }  *  */
end_comment

begin_class
DECL|class|LengthAssertion
specifier|public
class|class
name|LengthAssertion
extends|extends
name|Assertion
block|{
DECL|field|logger
specifier|private
specifier|static
specifier|final
name|ESLogger
name|logger
init|=
name|Loggers
operator|.
name|getLogger
argument_list|(
name|LengthAssertion
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|method|LengthAssertion
specifier|public
name|LengthAssertion
parameter_list|(
name|String
name|field
parameter_list|,
name|Object
name|expectedValue
parameter_list|)
block|{
name|super
argument_list|(
name|field
argument_list|,
name|expectedValue
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doAssert
specifier|protected
name|void
name|doAssert
parameter_list|(
name|Object
name|actualValue
parameter_list|,
name|Object
name|expectedValue
parameter_list|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"assert that [{}] has length [{}]"
argument_list|,
name|actualValue
argument_list|,
name|expectedValue
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|expectedValue
argument_list|,
name|instanceOf
argument_list|(
name|Number
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|int
name|length
init|=
operator|(
operator|(
name|Number
operator|)
name|expectedValue
operator|)
operator|.
name|intValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|actualValue
operator|instanceof
name|String
condition|)
block|{
name|assertThat
argument_list|(
name|errorMessage
argument_list|()
argument_list|,
operator|(
operator|(
name|String
operator|)
name|actualValue
operator|)
operator|.
name|length
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|length
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|actualValue
operator|instanceof
name|List
condition|)
block|{
name|assertThat
argument_list|(
name|errorMessage
argument_list|()
argument_list|,
operator|(
operator|(
name|List
operator|)
name|actualValue
operator|)
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|length
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|actualValue
operator|instanceof
name|Map
condition|)
block|{
name|assertThat
argument_list|(
name|errorMessage
argument_list|()
argument_list|,
operator|(
operator|(
name|Map
operator|)
name|actualValue
operator|)
operator|.
name|keySet
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|length
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"value is of unsupported type ["
operator|+
name|actualValue
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
DECL|method|errorMessage
specifier|private
name|String
name|errorMessage
parameter_list|()
block|{
return|return
literal|"field ["
operator|+
name|getField
argument_list|()
operator|+
literal|"] doesn't have length ["
operator|+
name|getExpectedValue
argument_list|()
operator|+
literal|"]"
return|;
block|}
block|}
end_class

end_unit

