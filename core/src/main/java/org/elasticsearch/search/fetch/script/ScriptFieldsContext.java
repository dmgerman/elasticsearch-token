begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.fetch.script
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|fetch
operator|.
name|script
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|SearchScript
import|;
end_import

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
name|List
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|ScriptFieldsContext
specifier|public
class|class
name|ScriptFieldsContext
block|{
DECL|class|ScriptField
specifier|public
specifier|static
class|class
name|ScriptField
block|{
DECL|field|name
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
DECL|field|script
specifier|private
specifier|final
name|SearchScript
name|script
decl_stmt|;
DECL|field|ignoreException
specifier|private
specifier|final
name|boolean
name|ignoreException
decl_stmt|;
DECL|method|ScriptField
specifier|public
name|ScriptField
parameter_list|(
name|String
name|name
parameter_list|,
name|SearchScript
name|script
parameter_list|,
name|boolean
name|ignoreException
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|script
operator|=
name|script
expr_stmt|;
name|this
operator|.
name|ignoreException
operator|=
name|ignoreException
expr_stmt|;
block|}
DECL|method|name
specifier|public
name|String
name|name
parameter_list|()
block|{
return|return
name|name
return|;
block|}
DECL|method|script
specifier|public
name|SearchScript
name|script
parameter_list|()
block|{
return|return
name|this
operator|.
name|script
return|;
block|}
DECL|method|ignoreException
specifier|public
name|boolean
name|ignoreException
parameter_list|()
block|{
return|return
name|ignoreException
return|;
block|}
block|}
DECL|field|fields
specifier|private
name|List
argument_list|<
name|ScriptField
argument_list|>
name|fields
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
DECL|method|ScriptFieldsContext
specifier|public
name|ScriptFieldsContext
parameter_list|()
block|{     }
DECL|method|add
specifier|public
name|void
name|add
parameter_list|(
name|ScriptField
name|field
parameter_list|)
block|{
name|this
operator|.
name|fields
operator|.
name|add
argument_list|(
name|field
argument_list|)
expr_stmt|;
block|}
DECL|method|fields
specifier|public
name|List
argument_list|<
name|ScriptField
argument_list|>
name|fields
parameter_list|()
block|{
return|return
name|this
operator|.
name|fields
return|;
block|}
block|}
end_class

end_unit

