begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.script.python
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|python
package|;
end_package

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
name|IndexReader
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
name|component
operator|.
name|AbstractComponent
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
name|inject
operator|.
name|Inject
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
name|ScriptEngineService
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

begin_import
import|import
name|org
operator|.
name|python
operator|.
name|core
operator|.
name|Py
import|;
end_import

begin_import
import|import
name|org
operator|.
name|python
operator|.
name|core
operator|.
name|PyCode
import|;
end_import

begin_import
import|import
name|org
operator|.
name|python
operator|.
name|core
operator|.
name|PyObject
import|;
end_import

begin_import
import|import
name|org
operator|.
name|python
operator|.
name|core
operator|.
name|PyStringMap
import|;
end_import

begin_import
import|import
name|org
operator|.
name|python
operator|.
name|util
operator|.
name|PythonInterpreter
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

begin_comment
comment|/**  *  */
end_comment

begin_comment
comment|//TODO we can optimize the case for Map<String, Object> similar to PyStringMap
end_comment

begin_class
DECL|class|PythonScriptEngineService
specifier|public
class|class
name|PythonScriptEngineService
extends|extends
name|AbstractComponent
implements|implements
name|ScriptEngineService
block|{
DECL|field|interp
specifier|private
specifier|final
name|PythonInterpreter
name|interp
decl_stmt|;
annotation|@
name|Inject
DECL|method|PythonScriptEngineService
specifier|public
name|PythonScriptEngineService
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|interp
operator|=
name|PythonInterpreter
operator|.
name|threadLocalStateInterpreter
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|types
specifier|public
name|String
index|[]
name|types
parameter_list|()
block|{
return|return
operator|new
name|String
index|[]
block|{
literal|"python"
block|,
literal|"py"
block|}
return|;
block|}
annotation|@
name|Override
DECL|method|extensions
specifier|public
name|String
index|[]
name|extensions
parameter_list|()
block|{
return|return
operator|new
name|String
index|[]
block|{
literal|"py"
block|}
return|;
block|}
annotation|@
name|Override
DECL|method|compile
specifier|public
name|Object
name|compile
parameter_list|(
name|String
name|script
parameter_list|)
block|{
return|return
name|interp
operator|.
name|compile
argument_list|(
name|script
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
name|Object
name|compiledScript
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|vars
parameter_list|)
block|{
return|return
operator|new
name|PythonExecutableScript
argument_list|(
operator|(
name|PyCode
operator|)
name|compiledScript
argument_list|,
name|vars
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|search
specifier|public
name|SearchScript
name|search
parameter_list|(
name|Object
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
name|vars
parameter_list|)
block|{
return|return
operator|new
name|PythonSearchScript
argument_list|(
operator|(
name|PyCode
operator|)
name|compiledScript
argument_list|,
name|vars
argument_list|,
name|lookup
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|execute
specifier|public
name|Object
name|execute
parameter_list|(
name|Object
name|compiledScript
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|vars
parameter_list|)
block|{
name|PyObject
name|pyVars
init|=
name|Py
operator|.
name|java2py
argument_list|(
name|vars
argument_list|)
decl_stmt|;
name|interp
operator|.
name|setLocals
argument_list|(
name|pyVars
argument_list|)
expr_stmt|;
name|PyObject
name|ret
init|=
name|interp
operator|.
name|eval
argument_list|(
operator|(
name|PyCode
operator|)
name|compiledScript
argument_list|)
decl_stmt|;
if|if
condition|(
name|ret
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|ret
operator|.
name|__tojava__
argument_list|(
name|Object
operator|.
name|class
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|unwrap
specifier|public
name|Object
name|unwrap
parameter_list|(
name|Object
name|value
parameter_list|)
block|{
return|return
name|unwrapValue
argument_list|(
name|value
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
block|{
name|interp
operator|.
name|cleanup
argument_list|()
expr_stmt|;
block|}
DECL|class|PythonExecutableScript
specifier|public
class|class
name|PythonExecutableScript
implements|implements
name|ExecutableScript
block|{
DECL|field|code
specifier|private
specifier|final
name|PyCode
name|code
decl_stmt|;
DECL|field|pyVars
specifier|private
specifier|final
name|PyStringMap
name|pyVars
decl_stmt|;
DECL|method|PythonExecutableScript
specifier|public
name|PythonExecutableScript
parameter_list|(
name|PyCode
name|code
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|vars
parameter_list|)
block|{
name|this
operator|.
name|code
operator|=
name|code
expr_stmt|;
name|this
operator|.
name|pyVars
operator|=
operator|new
name|PyStringMap
argument_list|()
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|entry
range|:
name|vars
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|pyVars
operator|.
name|__setitem__
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|Py
operator|.
name|java2py
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|setNextVar
specifier|public
name|void
name|setNextVar
parameter_list|(
name|String
name|name
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
name|pyVars
operator|.
name|__setitem__
argument_list|(
name|name
argument_list|,
name|Py
operator|.
name|java2py
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|run
specifier|public
name|Object
name|run
parameter_list|()
block|{
name|interp
operator|.
name|setLocals
argument_list|(
name|pyVars
argument_list|)
expr_stmt|;
name|PyObject
name|ret
init|=
name|interp
operator|.
name|eval
argument_list|(
name|code
argument_list|)
decl_stmt|;
if|if
condition|(
name|ret
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|ret
operator|.
name|__tojava__
argument_list|(
name|Object
operator|.
name|class
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|unwrap
specifier|public
name|Object
name|unwrap
parameter_list|(
name|Object
name|value
parameter_list|)
block|{
return|return
name|unwrapValue
argument_list|(
name|value
argument_list|)
return|;
block|}
block|}
DECL|class|PythonSearchScript
specifier|public
class|class
name|PythonSearchScript
implements|implements
name|SearchScript
block|{
DECL|field|code
specifier|private
specifier|final
name|PyCode
name|code
decl_stmt|;
DECL|field|pyVars
specifier|private
specifier|final
name|PyStringMap
name|pyVars
decl_stmt|;
DECL|field|lookup
specifier|private
specifier|final
name|SearchLookup
name|lookup
decl_stmt|;
DECL|method|PythonSearchScript
specifier|public
name|PythonSearchScript
parameter_list|(
name|PyCode
name|code
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|vars
parameter_list|,
name|SearchLookup
name|lookup
parameter_list|)
block|{
name|this
operator|.
name|code
operator|=
name|code
expr_stmt|;
name|this
operator|.
name|pyVars
operator|=
operator|new
name|PyStringMap
argument_list|()
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|entry
range|:
name|lookup
operator|.
name|asMap
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|pyVars
operator|.
name|__setitem__
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|Py
operator|.
name|java2py
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|vars
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|entry
range|:
name|vars
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|pyVars
operator|.
name|__setitem__
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|Py
operator|.
name|java2py
argument_list|(
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|this
operator|.
name|lookup
operator|=
name|lookup
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|setScorer
specifier|public
name|void
name|setScorer
parameter_list|(
name|Scorer
name|scorer
parameter_list|)
block|{
name|lookup
operator|.
name|setScorer
argument_list|(
name|scorer
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|setNextReader
specifier|public
name|void
name|setNextReader
parameter_list|(
name|IndexReader
name|reader
parameter_list|)
block|{
name|lookup
operator|.
name|setNextReader
argument_list|(
name|reader
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|setNextDocId
specifier|public
name|void
name|setNextDocId
parameter_list|(
name|int
name|doc
parameter_list|)
block|{
name|lookup
operator|.
name|setNextDocId
argument_list|(
name|doc
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|setNextSource
specifier|public
name|void
name|setNextSource
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|source
parameter_list|)
block|{
name|lookup
operator|.
name|source
argument_list|()
operator|.
name|setNextSource
argument_list|(
name|source
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|setNextScore
specifier|public
name|void
name|setNextScore
parameter_list|(
name|float
name|score
parameter_list|)
block|{
name|pyVars
operator|.
name|__setitem__
argument_list|(
literal|"_score"
argument_list|,
name|Py
operator|.
name|java2py
argument_list|(
name|score
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|setNextVar
specifier|public
name|void
name|setNextVar
parameter_list|(
name|String
name|name
parameter_list|,
name|Object
name|value
parameter_list|)
block|{
name|pyVars
operator|.
name|__setitem__
argument_list|(
name|name
argument_list|,
name|Py
operator|.
name|java2py
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|run
specifier|public
name|Object
name|run
parameter_list|()
block|{
name|interp
operator|.
name|setLocals
argument_list|(
name|pyVars
argument_list|)
expr_stmt|;
name|PyObject
name|ret
init|=
name|interp
operator|.
name|eval
argument_list|(
name|code
argument_list|)
decl_stmt|;
if|if
condition|(
name|ret
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|ret
operator|.
name|__tojava__
argument_list|(
name|Object
operator|.
name|class
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|runAsFloat
specifier|public
name|float
name|runAsFloat
parameter_list|()
block|{
return|return
operator|(
operator|(
name|Number
operator|)
name|run
argument_list|()
operator|)
operator|.
name|floatValue
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|runAsLong
specifier|public
name|long
name|runAsLong
parameter_list|()
block|{
return|return
operator|(
operator|(
name|Number
operator|)
name|run
argument_list|()
operator|)
operator|.
name|longValue
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|runAsDouble
specifier|public
name|double
name|runAsDouble
parameter_list|()
block|{
return|return
operator|(
operator|(
name|Number
operator|)
name|run
argument_list|()
operator|)
operator|.
name|doubleValue
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|unwrap
specifier|public
name|Object
name|unwrap
parameter_list|(
name|Object
name|value
parameter_list|)
block|{
return|return
name|unwrapValue
argument_list|(
name|value
argument_list|)
return|;
block|}
block|}
DECL|method|unwrapValue
specifier|public
specifier|static
name|Object
name|unwrapValue
parameter_list|(
name|Object
name|value
parameter_list|)
block|{
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
elseif|else
if|if
condition|(
name|value
operator|instanceof
name|PyObject
condition|)
block|{
comment|// seems like this is enough, inner PyDictionary will do the conversion for us for example, so expose it directly
return|return
operator|(
operator|(
name|PyObject
operator|)
name|value
operator|)
operator|.
name|__tojava__
argument_list|(
name|Object
operator|.
name|class
argument_list|)
return|;
block|}
return|return
name|value
return|;
block|}
block|}
end_class

end_unit

