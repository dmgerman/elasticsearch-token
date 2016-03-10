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
name|regex
operator|.
name|Pattern
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|hamcrest
operator|.
name|RegexMatcher
operator|.
name|matches
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
name|hamcrest
operator|.
name|Matchers
operator|.
name|notNullValue
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
comment|/**  * Represents a match assert section:  *  *   - match:   { get.fields._routing: "5" }  *  */
end_comment

begin_class
DECL|class|MatchAssertion
specifier|public
class|class
name|MatchAssertion
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
name|MatchAssertion
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|method|MatchAssertion
specifier|public
name|MatchAssertion
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
comment|//if the value is wrapped into / it is a regexp (e.g. /s+d+/)
if|if
condition|(
name|expectedValue
operator|instanceof
name|String
condition|)
block|{
name|String
name|expValue
init|=
operator|(
operator|(
name|String
operator|)
name|expectedValue
operator|)
operator|.
name|trim
argument_list|()
decl_stmt|;
if|if
condition|(
name|expValue
operator|.
name|length
argument_list|()
operator|>
literal|2
operator|&&
name|expValue
operator|.
name|startsWith
argument_list|(
literal|"/"
argument_list|)
operator|&&
name|expValue
operator|.
name|endsWith
argument_list|(
literal|"/"
argument_list|)
condition|)
block|{
name|assertThat
argument_list|(
literal|"field ["
operator|+
name|getField
argument_list|()
operator|+
literal|"] was expected to be of type String but is an instanceof ["
operator|+
name|safeClass
argument_list|(
name|actualValue
argument_list|)
operator|+
literal|"]"
argument_list|,
name|actualValue
argument_list|,
name|instanceOf
argument_list|(
name|String
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|stringValue
init|=
operator|(
name|String
operator|)
name|actualValue
decl_stmt|;
name|String
name|regex
init|=
name|expValue
operator|.
name|substring
argument_list|(
literal|1
argument_list|,
name|expValue
operator|.
name|length
argument_list|()
operator|-
literal|1
argument_list|)
decl_stmt|;
name|logger
operator|.
name|trace
argument_list|(
literal|"assert that [{}] matches [{}]"
argument_list|,
name|stringValue
argument_list|,
name|regex
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"field ["
operator|+
name|getField
argument_list|()
operator|+
literal|"] was expected to match the provided regex but didn't"
argument_list|,
name|stringValue
argument_list|,
name|matches
argument_list|(
name|regex
argument_list|,
name|Pattern
operator|.
name|COMMENTS
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
block|}
name|assertThat
argument_list|(
name|errorMessage
argument_list|()
argument_list|,
name|actualValue
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|logger
operator|.
name|trace
argument_list|(
literal|"assert that [{}] matches [{}] (field [{}])"
argument_list|,
name|actualValue
argument_list|,
name|expectedValue
argument_list|,
name|getField
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|actualValue
operator|.
name|getClass
argument_list|()
operator|.
name|equals
argument_list|(
name|safeClass
argument_list|(
name|expectedValue
argument_list|)
argument_list|)
condition|)
block|{
if|if
condition|(
name|actualValue
operator|instanceof
name|Number
operator|&&
name|expectedValue
operator|instanceof
name|Number
condition|)
block|{
comment|//Double 1.0 is equal to Integer 1
name|assertThat
argument_list|(
name|errorMessage
argument_list|()
argument_list|,
operator|(
operator|(
name|Number
operator|)
name|actualValue
operator|)
operator|.
name|doubleValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|(
operator|(
name|Number
operator|)
name|expectedValue
operator|)
operator|.
name|doubleValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
block|}
name|assertThat
argument_list|(
name|errorMessage
argument_list|()
argument_list|,
name|actualValue
argument_list|,
name|equalTo
argument_list|(
name|expectedValue
argument_list|)
argument_list|)
expr_stmt|;
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
literal|"] doesn't match the expected value"
return|;
block|}
block|}
end_class

end_unit
