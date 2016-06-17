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
name|cluster
operator|.
name|service
operator|.
name|ClusterService
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
name|Strings
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|core
operator|.
name|AbstractProcessor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|core
operator|.
name|AbstractProcessorFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|core
operator|.
name|IngestDocument
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|CompiledScript
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|ExecutableScript
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|Script
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|ScriptContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|ScriptService
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
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|emptyMap
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|Strings
operator|.
name|hasLength
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|core
operator|.
name|ConfigurationUtils
operator|.
name|newConfigurationException
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|core
operator|.
name|ConfigurationUtils
operator|.
name|readOptionalStringProperty
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|core
operator|.
name|ConfigurationUtils
operator|.
name|readStringProperty
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|ScriptService
operator|.
name|ScriptType
operator|.
name|FILE
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|ScriptService
operator|.
name|ScriptType
operator|.
name|INLINE
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|ScriptService
operator|.
name|ScriptType
operator|.
name|STORED
import|;
end_import

begin_comment
comment|/**  * Processor that adds new fields with their corresponding values. If the field is already present, its value  * will be replaced with the provided one.  */
end_comment

begin_class
DECL|class|ScriptProcessor
specifier|public
specifier|final
class|class
name|ScriptProcessor
extends|extends
name|AbstractProcessor
block|{
DECL|field|TYPE
specifier|public
specifier|static
specifier|final
name|String
name|TYPE
init|=
literal|"script"
decl_stmt|;
DECL|field|script
specifier|private
specifier|final
name|Script
name|script
decl_stmt|;
DECL|field|scriptService
specifier|private
specifier|final
name|ScriptService
name|scriptService
decl_stmt|;
DECL|field|clusterService
specifier|private
specifier|final
name|ClusterService
name|clusterService
decl_stmt|;
DECL|field|field
specifier|private
specifier|final
name|String
name|field
decl_stmt|;
DECL|method|ScriptProcessor
name|ScriptProcessor
parameter_list|(
name|String
name|tag
parameter_list|,
name|Script
name|script
parameter_list|,
name|ScriptService
name|scriptService
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|String
name|field
parameter_list|)
block|{
name|super
argument_list|(
name|tag
argument_list|)
expr_stmt|;
name|this
operator|.
name|script
operator|=
name|script
expr_stmt|;
name|this
operator|.
name|scriptService
operator|=
name|scriptService
expr_stmt|;
name|this
operator|.
name|clusterService
operator|=
name|clusterService
expr_stmt|;
name|this
operator|.
name|field
operator|=
name|field
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|execute
specifier|public
name|void
name|execute
parameter_list|(
name|IngestDocument
name|document
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|vars
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|vars
operator|.
name|put
argument_list|(
literal|"ctx"
argument_list|,
name|document
operator|.
name|getSourceAndMetadata
argument_list|()
argument_list|)
expr_stmt|;
name|CompiledScript
name|compiledScript
init|=
name|scriptService
operator|.
name|compile
argument_list|(
name|script
argument_list|,
name|ScriptContext
operator|.
name|Standard
operator|.
name|INGEST
argument_list|,
name|emptyMap
argument_list|()
argument_list|,
name|clusterService
operator|.
name|state
argument_list|()
argument_list|)
decl_stmt|;
name|ExecutableScript
name|executableScript
init|=
name|scriptService
operator|.
name|executable
argument_list|(
name|compiledScript
argument_list|,
name|vars
argument_list|)
decl_stmt|;
name|Object
name|value
init|=
name|executableScript
operator|.
name|run
argument_list|()
decl_stmt|;
if|if
condition|(
name|field
operator|!=
literal|null
condition|)
block|{
name|document
operator|.
name|setFieldValue
argument_list|(
name|field
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|getType
specifier|public
name|String
name|getType
parameter_list|()
block|{
return|return
name|TYPE
return|;
block|}
DECL|class|Factory
specifier|public
specifier|static
specifier|final
class|class
name|Factory
extends|extends
name|AbstractProcessorFactory
argument_list|<
name|ScriptProcessor
argument_list|>
block|{
DECL|field|scriptService
specifier|private
specifier|final
name|ScriptService
name|scriptService
decl_stmt|;
DECL|field|clusterService
specifier|private
specifier|final
name|ClusterService
name|clusterService
decl_stmt|;
DECL|method|Factory
specifier|public
name|Factory
parameter_list|(
name|ScriptService
name|scriptService
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|)
block|{
name|this
operator|.
name|scriptService
operator|=
name|scriptService
expr_stmt|;
name|this
operator|.
name|clusterService
operator|=
name|clusterService
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doCreate
specifier|public
name|ScriptProcessor
name|doCreate
parameter_list|(
name|String
name|processorTag
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|config
parameter_list|)
throws|throws
name|Exception
block|{
name|String
name|field
init|=
name|readOptionalStringProperty
argument_list|(
name|TYPE
argument_list|,
name|processorTag
argument_list|,
name|config
argument_list|,
literal|"field"
argument_list|)
decl_stmt|;
name|String
name|lang
init|=
name|readStringProperty
argument_list|(
name|TYPE
argument_list|,
name|processorTag
argument_list|,
name|config
argument_list|,
literal|"lang"
argument_list|)
decl_stmt|;
name|String
name|inline
init|=
name|readOptionalStringProperty
argument_list|(
name|TYPE
argument_list|,
name|processorTag
argument_list|,
name|config
argument_list|,
literal|"inline"
argument_list|)
decl_stmt|;
name|String
name|file
init|=
name|readOptionalStringProperty
argument_list|(
name|TYPE
argument_list|,
name|processorTag
argument_list|,
name|config
argument_list|,
literal|"file"
argument_list|)
decl_stmt|;
name|String
name|id
init|=
name|readOptionalStringProperty
argument_list|(
name|TYPE
argument_list|,
name|processorTag
argument_list|,
name|config
argument_list|,
literal|"id"
argument_list|)
decl_stmt|;
name|boolean
name|containsNoScript
init|=
operator|!
name|hasLength
argument_list|(
name|file
argument_list|)
operator|&&
operator|!
name|hasLength
argument_list|(
name|id
argument_list|)
operator|&&
operator|!
name|hasLength
argument_list|(
name|inline
argument_list|)
decl_stmt|;
if|if
condition|(
name|containsNoScript
condition|)
block|{
throw|throw
name|newConfigurationException
argument_list|(
name|TYPE
argument_list|,
name|processorTag
argument_list|,
literal|null
argument_list|,
literal|"Need [file], [id], or [inline] parameter to refer to scripts"
argument_list|)
throw|;
block|}
name|boolean
name|moreThanOneConfigured
init|=
operator|(
name|Strings
operator|.
name|hasLength
argument_list|(
name|file
argument_list|)
operator|&&
name|Strings
operator|.
name|hasLength
argument_list|(
name|id
argument_list|)
operator|)
operator|||
operator|(
name|Strings
operator|.
name|hasLength
argument_list|(
name|file
argument_list|)
operator|&&
name|Strings
operator|.
name|hasLength
argument_list|(
name|inline
argument_list|)
operator|)
operator|||
operator|(
name|Strings
operator|.
name|hasLength
argument_list|(
name|id
argument_list|)
operator|&&
name|Strings
operator|.
name|hasLength
argument_list|(
name|inline
argument_list|)
operator|)
decl_stmt|;
if|if
condition|(
name|moreThanOneConfigured
condition|)
block|{
throw|throw
name|newConfigurationException
argument_list|(
name|TYPE
argument_list|,
name|processorTag
argument_list|,
literal|null
argument_list|,
literal|"Only one of [file], [id], or [inline] may be configured"
argument_list|)
throw|;
block|}
specifier|final
name|Script
name|script
decl_stmt|;
if|if
condition|(
name|Strings
operator|.
name|hasLength
argument_list|(
name|file
argument_list|)
condition|)
block|{
name|script
operator|=
operator|new
name|Script
argument_list|(
name|file
argument_list|,
name|FILE
argument_list|,
name|lang
argument_list|,
name|emptyMap
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|Strings
operator|.
name|hasLength
argument_list|(
name|inline
argument_list|)
condition|)
block|{
name|script
operator|=
operator|new
name|Script
argument_list|(
name|inline
argument_list|,
name|INLINE
argument_list|,
name|lang
argument_list|,
name|emptyMap
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|Strings
operator|.
name|hasLength
argument_list|(
name|id
argument_list|)
condition|)
block|{
name|script
operator|=
operator|new
name|Script
argument_list|(
name|id
argument_list|,
name|STORED
argument_list|,
name|lang
argument_list|,
name|emptyMap
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
name|newConfigurationException
argument_list|(
name|TYPE
argument_list|,
name|processorTag
argument_list|,
literal|null
argument_list|,
literal|"Could not initialize script"
argument_list|)
throw|;
block|}
return|return
operator|new
name|ScriptProcessor
argument_list|(
name|processorTag
argument_list|,
name|script
argument_list|,
name|scriptService
argument_list|,
name|clusterService
argument_list|,
name|field
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

