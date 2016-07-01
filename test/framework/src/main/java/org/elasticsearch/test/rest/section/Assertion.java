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
name|test
operator|.
name|rest
operator|.
name|RestTestExecutionContext
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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

begin_comment
comment|/**  * Base class for executable sections that hold assertions  */
end_comment

begin_class
DECL|class|Assertion
specifier|public
specifier|abstract
class|class
name|Assertion
implements|implements
name|ExecutableSection
block|{
DECL|field|field
specifier|private
specifier|final
name|String
name|field
decl_stmt|;
DECL|field|expectedValue
specifier|private
specifier|final
name|Object
name|expectedValue
decl_stmt|;
DECL|method|Assertion
specifier|protected
name|Assertion
parameter_list|(
name|String
name|field
parameter_list|,
name|Object
name|expectedValue
parameter_list|)
block|{
name|this
operator|.
name|field
operator|=
name|field
expr_stmt|;
name|this
operator|.
name|expectedValue
operator|=
name|expectedValue
expr_stmt|;
block|}
DECL|method|getField
specifier|public
specifier|final
name|String
name|getField
parameter_list|()
block|{
return|return
name|field
return|;
block|}
DECL|method|getExpectedValue
specifier|public
specifier|final
name|Object
name|getExpectedValue
parameter_list|()
block|{
return|return
name|expectedValue
return|;
block|}
DECL|method|resolveExpectedValue
specifier|protected
specifier|final
name|Object
name|resolveExpectedValue
parameter_list|(
name|RestTestExecutionContext
name|executionContext
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|expectedValue
operator|instanceof
name|Map
condition|)
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|map
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|expectedValue
decl_stmt|;
return|return
name|executionContext
operator|.
name|stash
argument_list|()
operator|.
name|replaceStashedValues
argument_list|(
name|map
argument_list|)
return|;
block|}
if|if
condition|(
name|executionContext
operator|.
name|stash
argument_list|()
operator|.
name|isStashedValue
argument_list|(
name|expectedValue
argument_list|)
condition|)
block|{
return|return
name|executionContext
operator|.
name|stash
argument_list|()
operator|.
name|getValue
argument_list|(
name|expectedValue
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
return|return
name|expectedValue
return|;
block|}
DECL|method|getActualValue
specifier|protected
specifier|final
name|Object
name|getActualValue
parameter_list|(
name|RestTestExecutionContext
name|executionContext
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|executionContext
operator|.
name|stash
argument_list|()
operator|.
name|isStashedValue
argument_list|(
name|field
argument_list|)
condition|)
block|{
return|return
name|executionContext
operator|.
name|stash
argument_list|()
operator|.
name|getValue
argument_list|(
name|field
argument_list|)
return|;
block|}
return|return
name|executionContext
operator|.
name|response
argument_list|(
name|field
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|execute
specifier|public
specifier|final
name|void
name|execute
parameter_list|(
name|RestTestExecutionContext
name|executionContext
parameter_list|)
throws|throws
name|IOException
block|{
name|doAssert
argument_list|(
name|getActualValue
argument_list|(
name|executionContext
argument_list|)
argument_list|,
name|resolveExpectedValue
argument_list|(
name|executionContext
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * Executes the assertion comparing the actual value (parsed from the response) with the expected one      */
DECL|method|doAssert
specifier|protected
specifier|abstract
name|void
name|doAssert
parameter_list|(
name|Object
name|actualValue
parameter_list|,
name|Object
name|expectedValue
parameter_list|)
function_decl|;
comment|/**      * a utility to get the class of an object, protecting for null (i.e., returning null if the input is null)      */
DECL|method|safeClass
specifier|protected
name|Class
argument_list|<
name|?
argument_list|>
name|safeClass
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
return|return
name|o
operator|==
literal|null
condition|?
literal|null
else|:
name|o
operator|.
name|getClass
argument_list|()
return|;
block|}
block|}
end_class

end_unit

