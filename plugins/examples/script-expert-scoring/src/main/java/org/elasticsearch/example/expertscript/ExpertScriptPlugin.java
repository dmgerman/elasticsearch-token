begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.example.expertscript
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|example
operator|.
name|expertscript
package|;
end_package

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
name|UncheckedIOException
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
name|Objects
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
name|Function
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|LeafReaderContext
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|PostingsEnum
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|Term
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|Scorer
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
name|Nullable
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
name|settings
operator|.
name|Settings
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
name|org
operator|.
name|elasticsearch
operator|.
name|plugins
operator|.
name|ScriptPlugin
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
name|LeafSearchScript
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
name|ScriptEngine
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
name|SearchScript
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|lookup
operator|.
name|SearchLookup
import|;
end_import

begin_comment
comment|/**  * An example script plugin that adds a {@link ScriptEngine} implementing expert scoring.  */
end_comment

begin_class
DECL|class|ExpertScriptPlugin
specifier|public
class|class
name|ExpertScriptPlugin
extends|extends
name|Plugin
implements|implements
name|ScriptPlugin
block|{
annotation|@
name|Override
DECL|method|getScriptEngine
specifier|public
name|ScriptEngine
name|getScriptEngine
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
return|return
operator|new
name|MyExpertScriptEngine
argument_list|()
return|;
block|}
comment|/** An example {@link ScriptEngine} that uses Lucene segment details to implement pure document frequency scoring. */
comment|// tag::expert_engine
DECL|class|MyExpertScriptEngine
specifier|private
specifier|static
class|class
name|MyExpertScriptEngine
implements|implements
name|ScriptEngine
block|{
annotation|@
name|Override
DECL|method|getType
specifier|public
name|String
name|getType
parameter_list|()
block|{
return|return
literal|"expert_scripts"
return|;
block|}
annotation|@
name|Override
DECL|method|compile
specifier|public
name|Function
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|,
name|SearchScript
argument_list|>
name|compile
parameter_list|(
name|String
name|scriptName
parameter_list|,
name|String
name|scriptSource
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
parameter_list|)
block|{
comment|// we use the script "source" as the script identifier
if|if
condition|(
literal|"pure_df"
operator|.
name|equals
argument_list|(
name|scriptSource
argument_list|)
condition|)
block|{
return|return
name|p
lambda|->
operator|new
name|SearchScript
argument_list|()
block|{
name|final
name|String
name|field
return|;
specifier|final
name|String
name|term
decl_stmt|;
block|{
if|if
condition|(
name|p
operator|.
name|containsKey
argument_list|(
literal|"field"
argument_list|)
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Missing parameter [field]"
argument_list|)
throw|;
block|}
if|if
condition|(
name|p
operator|.
name|containsKey
argument_list|(
literal|"term"
argument_list|)
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Missing parameter [term]"
argument_list|)
throw|;
block|}
name|field
operator|=
name|p
operator|.
name|get
argument_list|(
literal|"field"
argument_list|)
operator|.
name|toString
argument_list|()
expr_stmt|;
name|term
operator|=
name|p
operator|.
name|get
argument_list|(
literal|"term"
argument_list|)
operator|.
name|toString
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|LeafSearchScript
name|getLeafSearchScript
parameter_list|(
name|LeafReaderContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|PostingsEnum
name|postings
init|=
name|context
operator|.
name|reader
argument_list|()
operator|.
name|postings
argument_list|(
operator|new
name|Term
argument_list|(
name|field
argument_list|,
name|term
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|postings
operator|==
literal|null
condition|)
block|{
comment|// the field and/or term don't exist in this segment, so always return 0
return|return
parameter_list|()
lambda|->
literal|0.0d
return|;
block|}
return|return
operator|new
name|LeafSearchScript
argument_list|()
block|{
name|int
name|currentDocid
init|=
operator|-
literal|1
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|setDocument
parameter_list|(
name|int
name|docid
parameter_list|)
block|{
comment|// advance has undefined behavior calling with a docid<= its current docid
if|if
condition|(
name|postings
operator|.
name|docID
argument_list|()
operator|<
name|docid
condition|)
block|{
try|try
block|{
name|postings
operator|.
name|advance
argument_list|(
name|docid
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|UncheckedIOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
name|currentDocid
operator|=
name|docid
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|double
name|runAsDouble
parameter_list|()
block|{
if|if
condition|(
name|postings
operator|.
name|docID
argument_list|()
operator|!=
name|currentDocid
condition|)
block|{
comment|// advance moved past the current doc, so this doc has no occurrences of the term
return|return
literal|0.0d
return|;
block|}
try|try
block|{
return|return
name|postings
operator|.
name|freq
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|UncheckedIOException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|needsScores
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
empty_stmt|;
block|}
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unknown script name "
operator|+
name|scriptSource
argument_list|)
throw|;
block|}
annotation|@
name|Override
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|search
specifier|public
name|SearchScript
name|search
parameter_list|(
name|CompiledScript
name|compiledScript
parameter_list|,
name|SearchLookup
name|lookup
parameter_list|,
annotation|@
name|Nullable
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
parameter_list|)
block|{
name|Function
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|,
name|SearchScript
argument_list|>
name|scriptFactory
init|=
operator|(
name|Function
argument_list|<
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|,
name|SearchScript
argument_list|>
operator|)
name|compiledScript
operator|.
name|compiled
argument_list|()
decl_stmt|;
return|return
name|scriptFactory
operator|.
name|apply
argument_list|(
name|params
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|executable
specifier|public
name|ExecutableScript
name|executable
parameter_list|(
name|CompiledScript
name|compiledScript
parameter_list|,
annotation|@
name|Nullable
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
annotation|@
name|Override
DECL|method|isInlineScriptEnabled
specifier|public
name|boolean
name|isInlineScriptEnabled
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{}
block|}
end_class

begin_comment
comment|// end::expert_engine
end_comment

unit|}
end_unit

