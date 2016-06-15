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
name|node
operator|.
name|NodeModule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|plugins
operator|.
name|Plugin
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|BufferedReader
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
name|io
operator|.
name|InputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|InputStreamReader
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|charset
operator|.
name|StandardCharsets
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
name|Map
import|;
end_import

begin_class
DECL|class|IngestCommonPlugin
specifier|public
class|class
name|IngestCommonPlugin
extends|extends
name|Plugin
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"ingest-common"
decl_stmt|;
DECL|field|builtinPatterns
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|builtinPatterns
decl_stmt|;
DECL|method|IngestCommonPlugin
specifier|public
name|IngestCommonPlugin
parameter_list|()
throws|throws
name|IOException
block|{
name|this
operator|.
name|builtinPatterns
operator|=
name|loadBuiltinPatterns
argument_list|()
expr_stmt|;
block|}
DECL|method|onModule
specifier|public
name|void
name|onModule
parameter_list|(
name|NodeModule
name|nodeModule
parameter_list|)
block|{
name|nodeModule
operator|.
name|registerProcessor
argument_list|(
name|DateProcessor
operator|.
name|TYPE
argument_list|,
parameter_list|(
name|registry
parameter_list|)
lambda|->
operator|new
name|DateProcessor
operator|.
name|Factory
argument_list|()
argument_list|)
expr_stmt|;
name|nodeModule
operator|.
name|registerProcessor
argument_list|(
name|SetProcessor
operator|.
name|TYPE
argument_list|,
parameter_list|(
name|registry
parameter_list|)
lambda|->
operator|new
name|SetProcessor
operator|.
name|Factory
argument_list|(
name|registry
operator|.
name|getTemplateService
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|nodeModule
operator|.
name|registerProcessor
argument_list|(
name|AppendProcessor
operator|.
name|TYPE
argument_list|,
parameter_list|(
name|registry
parameter_list|)
lambda|->
operator|new
name|AppendProcessor
operator|.
name|Factory
argument_list|(
name|registry
operator|.
name|getTemplateService
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|nodeModule
operator|.
name|registerProcessor
argument_list|(
name|RenameProcessor
operator|.
name|TYPE
argument_list|,
parameter_list|(
name|registry
parameter_list|)
lambda|->
operator|new
name|RenameProcessor
operator|.
name|Factory
argument_list|()
argument_list|)
expr_stmt|;
name|nodeModule
operator|.
name|registerProcessor
argument_list|(
name|RemoveProcessor
operator|.
name|TYPE
argument_list|,
parameter_list|(
name|registry
parameter_list|)
lambda|->
operator|new
name|RemoveProcessor
operator|.
name|Factory
argument_list|(
name|registry
operator|.
name|getTemplateService
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|nodeModule
operator|.
name|registerProcessor
argument_list|(
name|SplitProcessor
operator|.
name|TYPE
argument_list|,
parameter_list|(
name|registry
parameter_list|)
lambda|->
operator|new
name|SplitProcessor
operator|.
name|Factory
argument_list|()
argument_list|)
expr_stmt|;
name|nodeModule
operator|.
name|registerProcessor
argument_list|(
name|JoinProcessor
operator|.
name|TYPE
argument_list|,
parameter_list|(
name|registry
parameter_list|)
lambda|->
operator|new
name|JoinProcessor
operator|.
name|Factory
argument_list|()
argument_list|)
expr_stmt|;
name|nodeModule
operator|.
name|registerProcessor
argument_list|(
name|UppercaseProcessor
operator|.
name|TYPE
argument_list|,
parameter_list|(
name|registry
parameter_list|)
lambda|->
operator|new
name|UppercaseProcessor
operator|.
name|Factory
argument_list|()
argument_list|)
expr_stmt|;
name|nodeModule
operator|.
name|registerProcessor
argument_list|(
name|LowercaseProcessor
operator|.
name|TYPE
argument_list|,
parameter_list|(
name|registry
parameter_list|)
lambda|->
operator|new
name|LowercaseProcessor
operator|.
name|Factory
argument_list|()
argument_list|)
expr_stmt|;
name|nodeModule
operator|.
name|registerProcessor
argument_list|(
name|TrimProcessor
operator|.
name|TYPE
argument_list|,
parameter_list|(
name|registry
parameter_list|)
lambda|->
operator|new
name|TrimProcessor
operator|.
name|Factory
argument_list|()
argument_list|)
expr_stmt|;
name|nodeModule
operator|.
name|registerProcessor
argument_list|(
name|ConvertProcessor
operator|.
name|TYPE
argument_list|,
parameter_list|(
name|registry
parameter_list|)
lambda|->
operator|new
name|ConvertProcessor
operator|.
name|Factory
argument_list|()
argument_list|)
expr_stmt|;
name|nodeModule
operator|.
name|registerProcessor
argument_list|(
name|GsubProcessor
operator|.
name|TYPE
argument_list|,
parameter_list|(
name|registry
parameter_list|)
lambda|->
operator|new
name|GsubProcessor
operator|.
name|Factory
argument_list|()
argument_list|)
expr_stmt|;
name|nodeModule
operator|.
name|registerProcessor
argument_list|(
name|FailProcessor
operator|.
name|TYPE
argument_list|,
parameter_list|(
name|registry
parameter_list|)
lambda|->
operator|new
name|FailProcessor
operator|.
name|Factory
argument_list|(
name|registry
operator|.
name|getTemplateService
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|nodeModule
operator|.
name|registerProcessor
argument_list|(
name|ForEachProcessor
operator|.
name|TYPE
argument_list|,
parameter_list|(
name|registry
parameter_list|)
lambda|->
operator|new
name|ForEachProcessor
operator|.
name|Factory
argument_list|(
name|registry
argument_list|)
argument_list|)
expr_stmt|;
name|nodeModule
operator|.
name|registerProcessor
argument_list|(
name|DateIndexNameProcessor
operator|.
name|TYPE
argument_list|,
parameter_list|(
name|registry
parameter_list|)
lambda|->
operator|new
name|DateIndexNameProcessor
operator|.
name|Factory
argument_list|()
argument_list|)
expr_stmt|;
name|nodeModule
operator|.
name|registerProcessor
argument_list|(
name|SortProcessor
operator|.
name|TYPE
argument_list|,
parameter_list|(
name|registry
parameter_list|)
lambda|->
operator|new
name|SortProcessor
operator|.
name|Factory
argument_list|()
argument_list|)
expr_stmt|;
name|nodeModule
operator|.
name|registerProcessor
argument_list|(
name|GrokProcessor
operator|.
name|TYPE
argument_list|,
parameter_list|(
name|registry
parameter_list|)
lambda|->
operator|new
name|GrokProcessor
operator|.
name|Factory
argument_list|(
name|builtinPatterns
argument_list|)
argument_list|)
expr_stmt|;
name|nodeModule
operator|.
name|registerProcessor
argument_list|(
name|ScriptProcessor
operator|.
name|TYPE
argument_list|,
parameter_list|(
name|registry
parameter_list|)
lambda|->
operator|new
name|ScriptProcessor
operator|.
name|Factory
argument_list|(
name|registry
operator|.
name|getScriptService
argument_list|()
argument_list|,
name|registry
operator|.
name|getClusterService
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// Code for loading built-in grok patterns packaged with the jar file:
DECL|field|PATTERN_NAMES
specifier|private
specifier|static
specifier|final
name|String
index|[]
name|PATTERN_NAMES
init|=
operator|new
name|String
index|[]
block|{
literal|"aws"
block|,
literal|"bacula"
block|,
literal|"bro"
block|,
literal|"exim"
block|,
literal|"firewalls"
block|,
literal|"grok-patterns"
block|,
literal|"haproxy"
block|,
literal|"java"
block|,
literal|"junos"
block|,
literal|"linux-syslog"
block|,
literal|"mcollective-patterns"
block|,
literal|"mongodb"
block|,
literal|"nagios"
block|,
literal|"postgresql"
block|,
literal|"rails"
block|,
literal|"redis"
block|,
literal|"ruby"
block|}
decl_stmt|;
DECL|method|loadBuiltinPatterns
specifier|public
specifier|static
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|loadBuiltinPatterns
parameter_list|()
throws|throws
name|IOException
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|builtinPatterns
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|pattern
range|:
name|PATTERN_NAMES
control|)
block|{
try|try
init|(
name|InputStream
name|is
init|=
name|IngestCommonPlugin
operator|.
name|class
operator|.
name|getResourceAsStream
argument_list|(
literal|"/patterns/"
operator|+
name|pattern
argument_list|)
init|)
block|{
name|loadPatterns
argument_list|(
name|builtinPatterns
argument_list|,
name|is
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|Collections
operator|.
name|unmodifiableMap
argument_list|(
name|builtinPatterns
argument_list|)
return|;
block|}
DECL|method|loadPatterns
specifier|private
specifier|static
name|void
name|loadPatterns
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|patternBank
parameter_list|,
name|InputStream
name|inputStream
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|line
decl_stmt|;
name|BufferedReader
name|br
init|=
operator|new
name|BufferedReader
argument_list|(
operator|new
name|InputStreamReader
argument_list|(
name|inputStream
argument_list|,
name|StandardCharsets
operator|.
name|UTF_8
argument_list|)
argument_list|)
decl_stmt|;
while|while
condition|(
operator|(
name|line
operator|=
name|br
operator|.
name|readLine
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
name|String
name|trimmedLine
init|=
name|line
operator|.
name|replaceAll
argument_list|(
literal|"^\\s+"
argument_list|,
literal|""
argument_list|)
decl_stmt|;
if|if
condition|(
name|trimmedLine
operator|.
name|startsWith
argument_list|(
literal|"#"
argument_list|)
operator|||
name|trimmedLine
operator|.
name|length
argument_list|()
operator|==
literal|0
condition|)
block|{
continue|continue;
block|}
name|String
index|[]
name|parts
init|=
name|trimmedLine
operator|.
name|split
argument_list|(
literal|"\\s+"
argument_list|,
literal|2
argument_list|)
decl_stmt|;
if|if
condition|(
name|parts
operator|.
name|length
operator|==
literal|2
condition|)
block|{
name|patternBank
operator|.
name|put
argument_list|(
name|parts
index|[
literal|0
index|]
argument_list|,
name|parts
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

