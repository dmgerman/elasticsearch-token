begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.rest.yaml.section
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|rest
operator|.
name|yaml
operator|.
name|section
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|Logger
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
name|collect
operator|.
name|Tuple
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
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|XContentLocation
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
name|xcontent
operator|.
name|XContentParser
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
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|greaterThanOrEqualTo
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

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|fail
import|;
end_import

begin_comment
comment|/**  * Represents a gte assert section:  *  *   - gte:     { fields._ttl: 0 }  */
end_comment

begin_class
DECL|class|GreaterThanEqualToAssertion
specifier|public
class|class
name|GreaterThanEqualToAssertion
extends|extends
name|Assertion
block|{
DECL|method|parse
specifier|public
specifier|static
name|GreaterThanEqualToAssertion
name|parse
parameter_list|(
name|XContentParser
name|parser
parameter_list|)
throws|throws
name|IOException
block|{
name|XContentLocation
name|location
init|=
name|parser
operator|.
name|getTokenLocation
argument_list|()
decl_stmt|;
name|Tuple
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|stringObjectTuple
init|=
name|ParserUtils
operator|.
name|parseTuple
argument_list|(
name|parser
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
operator|(
name|stringObjectTuple
operator|.
name|v2
argument_list|()
operator|instanceof
name|Comparable
operator|)
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"gte section can only be used with objects that support natural ordering, found "
operator|+
name|stringObjectTuple
operator|.
name|v2
argument_list|()
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|)
throw|;
block|}
return|return
operator|new
name|GreaterThanEqualToAssertion
argument_list|(
name|location
argument_list|,
name|stringObjectTuple
operator|.
name|v1
argument_list|()
argument_list|,
name|stringObjectTuple
operator|.
name|v2
argument_list|()
argument_list|)
return|;
block|}
DECL|field|logger
specifier|private
specifier|static
specifier|final
name|Logger
name|logger
init|=
name|Loggers
operator|.
name|getLogger
argument_list|(
name|GreaterThanEqualToAssertion
operator|.
name|class
argument_list|)
decl_stmt|;
DECL|method|GreaterThanEqualToAssertion
specifier|public
name|GreaterThanEqualToAssertion
parameter_list|(
name|XContentLocation
name|location
parameter_list|,
name|String
name|field
parameter_list|,
name|Object
name|expectedValue
parameter_list|)
block|{
name|super
argument_list|(
name|location
argument_list|,
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
literal|"assert that [{}] is greater than or equal to [{}] (field: [{}])"
argument_list|,
name|actualValue
argument_list|,
name|expectedValue
argument_list|,
name|getField
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"value of ["
operator|+
name|getField
argument_list|()
operator|+
literal|"] is not comparable (got ["
operator|+
name|safeClass
argument_list|(
name|actualValue
argument_list|)
operator|+
literal|"])"
argument_list|,
name|actualValue
argument_list|,
name|instanceOf
argument_list|(
name|Comparable
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"expected value of ["
operator|+
name|getField
argument_list|()
operator|+
literal|"] is not comparable (got ["
operator|+
name|expectedValue
operator|.
name|getClass
argument_list|()
operator|+
literal|"])"
argument_list|,
name|expectedValue
argument_list|,
name|instanceOf
argument_list|(
name|Comparable
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|assertThat
argument_list|(
name|errorMessage
argument_list|()
argument_list|,
operator|(
name|Comparable
operator|)
name|actualValue
argument_list|,
name|greaterThanOrEqualTo
argument_list|(
operator|(
name|Comparable
operator|)
name|expectedValue
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassCastException
name|e
parameter_list|)
block|{
name|fail
argument_list|(
literal|"cast error while checking ("
operator|+
name|errorMessage
argument_list|()
operator|+
literal|"): "
operator|+
name|e
argument_list|)
expr_stmt|;
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
literal|"] is not greater than or equal to ["
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

