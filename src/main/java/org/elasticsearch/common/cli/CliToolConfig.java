begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.cli
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|cli
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|ImmutableMap
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|cli
operator|.
name|Option
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|cli
operator|.
name|OptionGroup
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|cli
operator|.
name|Options
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|CliToolConfig
specifier|public
class|class
name|CliToolConfig
block|{
DECL|method|config
specifier|public
specifier|static
name|Builder
name|config
parameter_list|(
name|String
name|name
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|CliTool
argument_list|>
name|toolType
parameter_list|)
block|{
return|return
operator|new
name|Builder
argument_list|(
name|name
argument_list|,
name|toolType
argument_list|)
return|;
block|}
DECL|field|toolType
specifier|private
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|CliTool
argument_list|>
name|toolType
decl_stmt|;
DECL|field|name
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
DECL|field|cmds
specifier|private
specifier|final
name|ImmutableMap
argument_list|<
name|String
argument_list|,
name|Cmd
argument_list|>
name|cmds
decl_stmt|;
DECL|field|helpPrinter
specifier|private
specifier|static
specifier|final
name|HelpPrinter
name|helpPrinter
init|=
operator|new
name|HelpPrinter
argument_list|()
decl_stmt|;
DECL|method|CliToolConfig
specifier|private
name|CliToolConfig
parameter_list|(
name|String
name|name
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|CliTool
argument_list|>
name|toolType
parameter_list|,
name|Cmd
index|[]
name|cmds
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
name|toolType
operator|=
name|toolType
expr_stmt|;
name|ImmutableMap
operator|.
name|Builder
argument_list|<
name|String
argument_list|,
name|Cmd
argument_list|>
name|cmdsBuilder
init|=
name|ImmutableMap
operator|.
name|builder
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|cmds
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|cmdsBuilder
operator|.
name|put
argument_list|(
name|cmds
index|[
name|i
index|]
operator|.
name|name
argument_list|,
name|cmds
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|cmds
operator|=
name|cmdsBuilder
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
DECL|method|isSingle
specifier|public
name|boolean
name|isSingle
parameter_list|()
block|{
return|return
name|cmds
operator|.
name|size
argument_list|()
operator|==
literal|1
return|;
block|}
DECL|method|single
specifier|public
name|Cmd
name|single
parameter_list|()
block|{
assert|assert
name|isSingle
argument_list|()
operator|:
literal|"Requesting single command on a multi-command tool"
assert|;
return|return
name|cmds
operator|.
name|values
argument_list|()
operator|.
name|iterator
argument_list|()
operator|.
name|next
argument_list|()
return|;
block|}
DECL|method|toolType
specifier|public
name|Class
argument_list|<
name|?
extends|extends
name|CliTool
argument_list|>
name|toolType
parameter_list|()
block|{
return|return
name|toolType
return|;
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
DECL|method|cmds
specifier|public
name|Collection
argument_list|<
name|Cmd
argument_list|>
name|cmds
parameter_list|()
block|{
return|return
name|cmds
operator|.
name|values
argument_list|()
return|;
block|}
DECL|method|cmd
specifier|public
name|Cmd
name|cmd
parameter_list|(
name|String
name|name
parameter_list|)
block|{
return|return
name|cmds
operator|.
name|get
argument_list|(
name|name
argument_list|)
return|;
block|}
DECL|method|printUsage
specifier|public
name|void
name|printUsage
parameter_list|(
name|Terminal
name|terminal
parameter_list|)
block|{
name|helpPrinter
operator|.
name|print
argument_list|(
name|this
argument_list|,
name|terminal
argument_list|)
expr_stmt|;
block|}
DECL|class|Builder
specifier|public
specifier|static
class|class
name|Builder
block|{
DECL|method|cmd
specifier|public
specifier|static
name|Cmd
operator|.
name|Builder
name|cmd
parameter_list|(
name|String
name|name
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|CliTool
operator|.
name|Command
argument_list|>
name|cmdType
parameter_list|)
block|{
return|return
operator|new
name|Cmd
operator|.
name|Builder
argument_list|(
name|name
argument_list|,
name|cmdType
argument_list|)
return|;
block|}
DECL|method|option
specifier|public
specifier|static
name|OptionBuilder
name|option
parameter_list|(
name|String
name|shortName
parameter_list|,
name|String
name|longName
parameter_list|)
block|{
return|return
operator|new
name|OptionBuilder
argument_list|(
name|shortName
argument_list|,
name|longName
argument_list|)
return|;
block|}
DECL|method|optionGroup
specifier|public
specifier|static
name|OptionGroupBuilder
name|optionGroup
parameter_list|(
name|boolean
name|required
parameter_list|)
block|{
return|return
operator|new
name|OptionGroupBuilder
argument_list|(
name|required
argument_list|)
return|;
block|}
DECL|field|toolType
specifier|private
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|CliTool
argument_list|>
name|toolType
decl_stmt|;
DECL|field|name
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
DECL|field|cmds
specifier|private
name|Cmd
index|[]
name|cmds
decl_stmt|;
DECL|method|Builder
specifier|private
name|Builder
parameter_list|(
name|String
name|name
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|CliTool
argument_list|>
name|toolType
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
name|toolType
operator|=
name|toolType
expr_stmt|;
block|}
DECL|method|cmds
specifier|public
name|Builder
name|cmds
parameter_list|(
name|Cmd
operator|.
name|Builder
modifier|...
name|cmds
parameter_list|)
block|{
name|this
operator|.
name|cmds
operator|=
operator|new
name|Cmd
index|[
name|cmds
operator|.
name|length
index|]
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|cmds
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|this
operator|.
name|cmds
index|[
name|i
index|]
operator|=
name|cmds
index|[
name|i
index|]
operator|.
name|build
argument_list|()
expr_stmt|;
name|this
operator|.
name|cmds
index|[
name|i
index|]
operator|.
name|toolName
operator|=
name|name
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
DECL|method|cmds
specifier|public
name|Builder
name|cmds
parameter_list|(
name|Cmd
modifier|...
name|cmds
parameter_list|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|cmds
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|cmds
index|[
name|i
index|]
operator|.
name|toolName
operator|=
name|name
expr_stmt|;
block|}
name|this
operator|.
name|cmds
operator|=
name|cmds
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|build
specifier|public
name|CliToolConfig
name|build
parameter_list|()
block|{
return|return
operator|new
name|CliToolConfig
argument_list|(
name|name
argument_list|,
name|toolType
argument_list|,
name|cmds
argument_list|)
return|;
block|}
block|}
DECL|class|Cmd
specifier|public
specifier|static
class|class
name|Cmd
block|{
DECL|field|toolName
specifier|private
name|String
name|toolName
decl_stmt|;
DECL|field|name
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
DECL|field|cmdType
specifier|private
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|CliTool
operator|.
name|Command
argument_list|>
name|cmdType
decl_stmt|;
DECL|field|options
specifier|private
specifier|final
name|Options
name|options
decl_stmt|;
DECL|method|Cmd
specifier|private
name|Cmd
parameter_list|(
name|String
name|name
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|CliTool
operator|.
name|Command
argument_list|>
name|cmdType
parameter_list|,
name|Options
name|options
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
name|cmdType
operator|=
name|cmdType
expr_stmt|;
name|this
operator|.
name|options
operator|=
name|options
expr_stmt|;
name|OptionsSource
operator|.
name|VERBOSITY
operator|.
name|populate
argument_list|(
name|options
argument_list|)
expr_stmt|;
block|}
DECL|method|cmdType
specifier|public
name|Class
argument_list|<
name|?
extends|extends
name|CliTool
operator|.
name|Command
argument_list|>
name|cmdType
parameter_list|()
block|{
return|return
name|cmdType
return|;
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
DECL|method|options
specifier|public
name|Options
name|options
parameter_list|()
block|{
return|return
name|options
return|;
block|}
DECL|method|printUsage
specifier|public
name|void
name|printUsage
parameter_list|(
name|Terminal
name|terminal
parameter_list|)
block|{
name|helpPrinter
operator|.
name|print
argument_list|(
name|toolName
argument_list|,
name|this
argument_list|,
name|terminal
argument_list|)
expr_stmt|;
block|}
DECL|class|Builder
specifier|public
specifier|static
class|class
name|Builder
block|{
DECL|field|name
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
DECL|field|cmdType
specifier|private
specifier|final
name|Class
argument_list|<
name|?
extends|extends
name|CliTool
operator|.
name|Command
argument_list|>
name|cmdType
decl_stmt|;
DECL|field|options
specifier|private
name|Options
name|options
init|=
operator|new
name|Options
argument_list|()
decl_stmt|;
DECL|method|Builder
specifier|private
name|Builder
parameter_list|(
name|String
name|name
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|CliTool
operator|.
name|Command
argument_list|>
name|cmdType
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
name|cmdType
operator|=
name|cmdType
expr_stmt|;
block|}
DECL|method|options
specifier|public
name|Builder
name|options
parameter_list|(
name|OptionBuilder
modifier|...
name|optionBuilder
parameter_list|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|optionBuilder
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|options
operator|.
name|addOption
argument_list|(
name|optionBuilder
index|[
name|i
index|]
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
DECL|method|optionGroups
specifier|public
name|Builder
name|optionGroups
parameter_list|(
name|OptionGroupBuilder
modifier|...
name|optionGroupBuilders
parameter_list|)
block|{
for|for
control|(
name|OptionGroupBuilder
name|builder
range|:
name|optionGroupBuilders
control|)
block|{
name|options
operator|.
name|addOptionGroup
argument_list|(
name|builder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
DECL|method|build
specifier|public
name|Cmd
name|build
parameter_list|()
block|{
return|return
operator|new
name|Cmd
argument_list|(
name|name
argument_list|,
name|cmdType
argument_list|,
name|options
argument_list|)
return|;
block|}
block|}
block|}
DECL|class|OptionBuilder
specifier|public
specifier|static
class|class
name|OptionBuilder
block|{
DECL|field|option
specifier|private
specifier|final
name|Option
name|option
decl_stmt|;
DECL|method|OptionBuilder
specifier|private
name|OptionBuilder
parameter_list|(
name|String
name|shortName
parameter_list|,
name|String
name|longName
parameter_list|)
block|{
name|option
operator|=
operator|new
name|Option
argument_list|(
name|shortName
argument_list|,
literal|""
argument_list|)
expr_stmt|;
name|option
operator|.
name|setLongOpt
argument_list|(
name|longName
argument_list|)
expr_stmt|;
name|option
operator|.
name|setArgName
argument_list|(
name|longName
argument_list|)
expr_stmt|;
block|}
DECL|method|required
specifier|public
name|OptionBuilder
name|required
parameter_list|(
name|boolean
name|required
parameter_list|)
block|{
name|option
operator|.
name|setRequired
argument_list|(
name|required
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|hasArg
specifier|public
name|OptionBuilder
name|hasArg
parameter_list|(
name|boolean
name|optional
parameter_list|)
block|{
name|option
operator|.
name|setOptionalArg
argument_list|(
name|optional
argument_list|)
expr_stmt|;
name|option
operator|.
name|setArgs
argument_list|(
literal|1
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|build
specifier|public
name|Option
name|build
parameter_list|()
block|{
return|return
name|option
return|;
block|}
block|}
DECL|class|OptionGroupBuilder
specifier|public
specifier|static
class|class
name|OptionGroupBuilder
block|{
DECL|field|group
specifier|private
name|OptionGroup
name|group
decl_stmt|;
DECL|method|OptionGroupBuilder
specifier|private
name|OptionGroupBuilder
parameter_list|(
name|boolean
name|required
parameter_list|)
block|{
name|group
operator|=
operator|new
name|OptionGroup
argument_list|()
expr_stmt|;
name|group
operator|.
name|setRequired
argument_list|(
name|required
argument_list|)
expr_stmt|;
block|}
DECL|method|options
specifier|public
name|OptionGroupBuilder
name|options
parameter_list|(
name|OptionBuilder
modifier|...
name|optionBuilders
parameter_list|)
block|{
for|for
control|(
name|OptionBuilder
name|builder
range|:
name|optionBuilders
control|)
block|{
name|group
operator|.
name|addOption
argument_list|(
name|builder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
DECL|method|build
specifier|public
name|OptionGroup
name|build
parameter_list|()
block|{
return|return
name|group
return|;
block|}
block|}
DECL|class|OptionsSource
specifier|static
specifier|abstract
class|class
name|OptionsSource
block|{
DECL|field|HELP
specifier|static
specifier|final
name|OptionsSource
name|HELP
init|=
operator|new
name|OptionsSource
argument_list|()
block|{
annotation|@
name|Override
name|void
name|populate
parameter_list|(
name|Options
name|options
parameter_list|)
block|{
name|options
operator|.
name|addOption
argument_list|(
operator|new
name|OptionBuilder
argument_list|(
literal|"h"
argument_list|,
literal|"help"
argument_list|)
operator|.
name|required
argument_list|(
literal|false
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
DECL|field|VERBOSITY
specifier|static
specifier|final
name|OptionsSource
name|VERBOSITY
init|=
operator|new
name|OptionsSource
argument_list|()
block|{
annotation|@
name|Override
name|void
name|populate
parameter_list|(
name|Options
name|options
parameter_list|)
block|{
name|OptionGroup
name|verbosityGroup
init|=
operator|new
name|OptionGroup
argument_list|()
decl_stmt|;
name|verbosityGroup
operator|.
name|setRequired
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|verbosityGroup
operator|.
name|addOption
argument_list|(
operator|new
name|OptionBuilder
argument_list|(
literal|"s"
argument_list|,
literal|"silent"
argument_list|)
operator|.
name|required
argument_list|(
literal|false
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|verbosityGroup
operator|.
name|addOption
argument_list|(
operator|new
name|OptionBuilder
argument_list|(
literal|"v"
argument_list|,
literal|"verbose"
argument_list|)
operator|.
name|required
argument_list|(
literal|false
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|options
operator|.
name|addOptionGroup
argument_list|(
name|verbosityGroup
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
DECL|field|options
specifier|private
name|Options
name|options
decl_stmt|;
DECL|method|options
name|Options
name|options
parameter_list|()
block|{
if|if
condition|(
name|options
operator|==
literal|null
condition|)
block|{
name|options
operator|=
operator|new
name|Options
argument_list|()
expr_stmt|;
name|populate
argument_list|(
name|options
argument_list|)
expr_stmt|;
block|}
return|return
name|options
return|;
block|}
DECL|method|populate
specifier|abstract
name|void
name|populate
parameter_list|(
name|Options
name|options
parameter_list|)
function_decl|;
block|}
block|}
end_class

end_unit

