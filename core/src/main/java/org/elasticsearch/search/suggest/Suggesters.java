begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.suggest
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
package|;
end_package

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
comment|/**  * Registry of Suggesters. This is only its own class to make Guice happy.  */
end_comment

begin_class
DECL|class|Suggesters
specifier|public
specifier|final
class|class
name|Suggesters
block|{
DECL|field|suggesters
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Suggester
argument_list|<
name|?
argument_list|>
argument_list|>
name|suggesters
decl_stmt|;
DECL|method|Suggesters
specifier|public
name|Suggesters
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Suggester
argument_list|<
name|?
argument_list|>
argument_list|>
name|suggesters
parameter_list|)
block|{
name|this
operator|.
name|suggesters
operator|=
name|suggesters
expr_stmt|;
block|}
DECL|method|getSuggester
specifier|public
name|Suggester
argument_list|<
name|?
argument_list|>
name|getSuggester
parameter_list|(
name|String
name|suggesterName
parameter_list|)
block|{
name|Suggester
argument_list|<
name|?
argument_list|>
name|suggester
init|=
name|suggesters
operator|.
name|get
argument_list|(
name|suggesterName
argument_list|)
decl_stmt|;
if|if
condition|(
name|suggester
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"suggester with name ["
operator|+
name|suggesterName
operator|+
literal|"] not supported"
argument_list|)
throw|;
block|}
return|return
name|suggester
return|;
block|}
block|}
end_class

end_unit

