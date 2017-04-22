begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.ingest.common
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|common
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
name|IngestDocument
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
name|Processor
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
name|ScriptException
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
name|ConfigurationUtils
operator|.
name|readOptionalMap
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
name|script
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
name|ScriptType
operator|.
name|STORED
import|;
end_import

begin_comment
comment|/**  * Processor that evaluates a script with an ingest document in its context.  */
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
comment|/**      * Processor that evaluates a script with an ingest document in its context      *      * @param tag The processor's tag.      * @param script The {@link Script} to execute.      * @param scriptService The {@link ScriptService} used to execute the script.      */
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
block|}
comment|/**      * Executes the script with the Ingest document in context.      *      * @param document The Ingest document passed into the script context under the "ctx" object.      */
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
name|script
operator|.
name|getParams
argument_list|()
argument_list|)
decl_stmt|;
name|executableScript
operator|.
name|setNextVar
argument_list|(
literal|"ctx"
argument_list|,
name|document
operator|.
name|getSourceAndMetadata
argument_list|()
argument_list|)
expr_stmt|;
name|executableScript
operator|.
name|run
argument_list|()
expr_stmt|;
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
DECL|method|getScript
name|Script
name|getScript
parameter_list|()
block|{
return|return
name|script
return|;
block|}
DECL|class|Factory
specifier|public
specifier|static
specifier|final
class|class
name|Factory
implements|implements
name|Processor
operator|.
name|Factory
block|{
DECL|field|scriptService
specifier|private
specifier|final
name|ScriptService
name|scriptService
decl_stmt|;
DECL|method|Factory
specifier|public
name|Factory
parameter_list|(
name|ScriptService
name|scriptService
parameter_list|)
block|{
name|this
operator|.
name|scriptService
operator|=
name|scriptService
expr_stmt|;
block|}
annotation|@
name|Override
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|create
specifier|public
name|ScriptProcessor
name|create
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Processor
operator|.
name|Factory
argument_list|>
name|registry
parameter_list|,
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
name|lang
init|=
name|readOptionalStringProperty
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
name|Map
argument_list|<
name|String
argument_list|,
name|?
argument_list|>
name|params
init|=
name|readOptionalMap
argument_list|(
name|TYPE
argument_list|,
name|processorTag
argument_list|,
name|config
argument_list|,
literal|"params"
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
if|if
condition|(
name|lang
operator|==
literal|null
condition|)
block|{
name|lang
operator|=
name|Script
operator|.
name|DEFAULT_SCRIPT_LANG
expr_stmt|;
block|}
if|if
condition|(
name|params
operator|==
literal|null
condition|)
block|{
name|params
operator|=
name|emptyMap
argument_list|()
expr_stmt|;
block|}
specifier|final
name|Script
name|script
decl_stmt|;
name|String
name|scriptPropertyUsed
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
name|FILE
argument_list|,
name|lang
argument_list|,
name|file
argument_list|,
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|params
argument_list|)
expr_stmt|;
name|scriptPropertyUsed
operator|=
literal|"file"
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
name|INLINE
argument_list|,
name|lang
argument_list|,
name|inline
argument_list|,
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|params
argument_list|)
expr_stmt|;
name|scriptPropertyUsed
operator|=
literal|"inline"
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
name|STORED
argument_list|,
name|lang
argument_list|,
name|id
argument_list|,
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|params
argument_list|)
expr_stmt|;
name|scriptPropertyUsed
operator|=
literal|"id"
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
comment|// verify script is able to be compiled before successfully creating processor.
try|try
block|{
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
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ScriptException
name|e
parameter_list|)
block|{
throw|throw
name|newConfigurationException
argument_list|(
name|TYPE
argument_list|,
name|processorTag
argument_list|,
name|scriptPropertyUsed
argument_list|,
name|e
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
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

