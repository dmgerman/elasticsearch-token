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
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|unmodifiableMap
import|;
end_import

begin_comment
comment|/**  * Represents a test fragment that contains the information needed to call an api  */
end_comment

begin_class
DECL|class|ApiCallSection
specifier|public
class|class
name|ApiCallSection
block|{
DECL|field|api
specifier|private
specifier|final
name|String
name|api
decl_stmt|;
DECL|field|params
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|headers
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|headers
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|bodies
specifier|private
specifier|final
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
name|bodies
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
DECL|method|ApiCallSection
specifier|public
name|ApiCallSection
parameter_list|(
name|String
name|api
parameter_list|)
block|{
name|this
operator|.
name|api
operator|=
name|api
expr_stmt|;
block|}
DECL|method|getApi
specifier|public
name|String
name|getApi
parameter_list|()
block|{
return|return
name|api
return|;
block|}
DECL|method|getParams
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getParams
parameter_list|()
block|{
comment|//make sure we never modify the parameters once returned
return|return
name|unmodifiableMap
argument_list|(
name|params
argument_list|)
return|;
block|}
DECL|method|addParam
specifier|public
name|void
name|addParam
parameter_list|(
name|String
name|key
parameter_list|,
name|String
name|value
parameter_list|)
block|{
name|String
name|existingValue
init|=
name|params
operator|.
name|get
argument_list|(
name|key
argument_list|)
decl_stmt|;
if|if
condition|(
name|existingValue
operator|!=
literal|null
condition|)
block|{
name|value
operator|=
name|existingValue
operator|+
literal|","
operator|+
name|value
expr_stmt|;
block|}
name|this
operator|.
name|params
operator|.
name|put
argument_list|(
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
DECL|method|addHeaders
specifier|public
name|void
name|addHeaders
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|otherHeaders
parameter_list|)
block|{
name|this
operator|.
name|headers
operator|.
name|putAll
argument_list|(
name|otherHeaders
argument_list|)
expr_stmt|;
block|}
DECL|method|getHeaders
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getHeaders
parameter_list|()
block|{
return|return
name|unmodifiableMap
argument_list|(
name|headers
argument_list|)
return|;
block|}
DECL|method|getBodies
specifier|public
name|List
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|>
name|getBodies
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|bodies
argument_list|)
return|;
block|}
DECL|method|addBody
specifier|public
name|void
name|addBody
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|body
parameter_list|)
block|{
name|this
operator|.
name|bodies
operator|.
name|add
argument_list|(
name|body
argument_list|)
expr_stmt|;
block|}
DECL|method|hasBody
specifier|public
name|boolean
name|hasBody
parameter_list|()
block|{
return|return
name|bodies
operator|.
name|size
argument_list|()
operator|>
literal|0
return|;
block|}
block|}
end_class

end_unit

