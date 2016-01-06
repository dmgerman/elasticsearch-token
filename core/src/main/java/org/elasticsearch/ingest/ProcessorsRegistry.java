begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.ingest
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|env
operator|.
name|Environment
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|BiFunction
import|;
end_import

begin_class
DECL|class|ProcessorsRegistry
specifier|public
class|class
name|ProcessorsRegistry
block|{
DECL|field|processorFactoryProviders
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|BiFunction
argument_list|<
name|Environment
argument_list|,
name|TemplateService
argument_list|,
name|Processor
operator|.
name|Factory
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|>
name|processorFactoryProviders
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
comment|/**      * Adds a processor factory under a specific name.      */
DECL|method|addProcessor
specifier|public
name|void
name|addProcessor
parameter_list|(
name|String
name|name
parameter_list|,
name|BiFunction
argument_list|<
name|Environment
argument_list|,
name|TemplateService
argument_list|,
name|Processor
operator|.
name|Factory
argument_list|<
name|?
argument_list|>
argument_list|>
name|processorFactoryProvider
parameter_list|)
block|{
name|BiFunction
argument_list|<
name|Environment
argument_list|,
name|TemplateService
argument_list|,
name|Processor
operator|.
name|Factory
argument_list|<
name|?
argument_list|>
argument_list|>
name|provider
init|=
name|processorFactoryProviders
operator|.
name|putIfAbsent
argument_list|(
name|name
argument_list|,
name|processorFactoryProvider
argument_list|)
decl_stmt|;
if|if
condition|(
name|provider
operator|!=
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Processor factory already registered for name ["
operator|+
name|name
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
DECL|method|entrySet
specifier|public
name|Set
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|BiFunction
argument_list|<
name|Environment
argument_list|,
name|TemplateService
argument_list|,
name|Processor
operator|.
name|Factory
argument_list|<
name|?
argument_list|>
argument_list|>
argument_list|>
argument_list|>
name|entrySet
parameter_list|()
block|{
return|return
name|processorFactoryProviders
operator|.
name|entrySet
argument_list|()
return|;
block|}
block|}
end_class

end_unit

