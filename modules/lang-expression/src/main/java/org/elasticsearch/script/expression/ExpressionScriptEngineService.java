begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.script.expression
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|expression
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
name|expressions
operator|.
name|Expression
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
name|expressions
operator|.
name|SimpleBindings
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
name|expressions
operator|.
name|js
operator|.
name|JavascriptCompiler
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
name|expressions
operator|.
name|js
operator|.
name|VariableContext
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
name|queries
operator|.
name|function
operator|.
name|ValueSource
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
name|queries
operator|.
name|function
operator|.
name|valuesource
operator|.
name|DoubleConstValueSource
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
name|SortField
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|SpecialPermission
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
name|index
operator|.
name|fielddata
operator|.
name|IndexFieldData
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|fielddata
operator|.
name|IndexNumericFieldData
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|MappedFieldType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|MapperService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|core
operator|.
name|DateFieldMapper
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|core
operator|.
name|LegacyDateFieldMapper
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|geo
operator|.
name|BaseGeoPointFieldMapper
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
name|ClassPermission
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
name|java
operator|.
name|security
operator|.
name|AccessControlContext
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|AccessController
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|PrivilegedAction
import|;
end_import

begin_import
import|import
name|java
operator|.
name|text
operator|.
name|ParseException
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
comment|/**  * Provides the infrastructure for Lucene expressions as a scripting language for Elasticsearch.  Only  * {@link SearchScript}s are supported.  */
end_comment

begin_class
DECL|class|ExpressionScriptEngineService
specifier|public
class|class
name|ExpressionScriptEngineService
extends|extends
name|AbstractComponent
implements|implements
name|ScriptEngineService
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"expression"
decl_stmt|;
DECL|method|ExpressionScriptEngineService
specifier|public
name|ExpressionScriptEngineService
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
name|NAME
return|;
block|}
annotation|@
name|Override
DECL|method|getExtension
specifier|public
name|String
name|getExtension
parameter_list|()
block|{
return|return
name|NAME
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
comment|// classloader created here
specifier|final
name|SecurityManager
name|sm
init|=
name|System
operator|.
name|getSecurityManager
argument_list|()
decl_stmt|;
if|if
condition|(
name|sm
operator|!=
literal|null
condition|)
block|{
name|sm
operator|.
name|checkPermission
argument_list|(
operator|new
name|SpecialPermission
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|AccessController
operator|.
name|doPrivileged
argument_list|(
operator|new
name|PrivilegedAction
argument_list|<
name|Expression
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|Expression
name|run
parameter_list|()
block|{
try|try
block|{
comment|// snapshot our context here, we check on behalf of the expression
name|AccessControlContext
name|engineContext
init|=
name|AccessController
operator|.
name|getContext
argument_list|()
decl_stmt|;
name|ClassLoader
name|loader
init|=
name|getClass
argument_list|()
operator|.
name|getClassLoader
argument_list|()
decl_stmt|;
if|if
condition|(
name|sm
operator|!=
literal|null
condition|)
block|{
name|loader
operator|=
operator|new
name|ClassLoader
argument_list|(
name|loader
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|Class
argument_list|<
name|?
argument_list|>
name|loadClass
parameter_list|(
name|String
name|name
parameter_list|,
name|boolean
name|resolve
parameter_list|)
throws|throws
name|ClassNotFoundException
block|{
try|try
block|{
name|engineContext
operator|.
name|checkPermission
argument_list|(
operator|new
name|ClassPermission
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SecurityException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ClassNotFoundException
argument_list|(
name|name
argument_list|,
name|e
argument_list|)
throw|;
block|}
return|return
name|super
operator|.
name|loadClass
argument_list|(
name|name
argument_list|,
name|resolve
argument_list|)
return|;
block|}
block|}
expr_stmt|;
block|}
comment|// NOTE: validation is delayed to allow runtime vars, and we don't have access to per index stuff here
return|return
name|JavascriptCompiler
operator|.
name|compile
argument_list|(
name|scriptSource
argument_list|,
name|JavascriptCompiler
operator|.
name|DEFAULT_FUNCTIONS
argument_list|,
name|loader
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|ParseException
name|e
parameter_list|)
block|{
throw|throw
name|convertToScriptException
argument_list|(
literal|"compile error"
argument_list|,
name|scriptSource
argument_list|,
name|scriptSource
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
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
name|vars
parameter_list|)
block|{
name|Expression
name|expr
init|=
operator|(
name|Expression
operator|)
name|compiledScript
operator|.
name|compiled
argument_list|()
decl_stmt|;
name|MapperService
name|mapper
init|=
name|lookup
operator|.
name|doc
argument_list|()
operator|.
name|mapperService
argument_list|()
decl_stmt|;
comment|// NOTE: if we need to do anything complicated with bindings in the future, we can just extend Bindings,
comment|// instead of complicating SimpleBindings (which should stay simple)
name|SimpleBindings
name|bindings
init|=
operator|new
name|SimpleBindings
argument_list|()
decl_stmt|;
name|ReplaceableConstValueSource
name|specialValue
init|=
literal|null
decl_stmt|;
for|for
control|(
name|String
name|variable
range|:
name|expr
operator|.
name|variables
control|)
block|{
try|try
block|{
if|if
condition|(
name|variable
operator|.
name|equals
argument_list|(
literal|"_score"
argument_list|)
condition|)
block|{
name|bindings
operator|.
name|add
argument_list|(
operator|new
name|SortField
argument_list|(
literal|"_score"
argument_list|,
name|SortField
operator|.
name|Type
operator|.
name|SCORE
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|variable
operator|.
name|equals
argument_list|(
literal|"_value"
argument_list|)
condition|)
block|{
name|specialValue
operator|=
operator|new
name|ReplaceableConstValueSource
argument_list|()
expr_stmt|;
name|bindings
operator|.
name|add
argument_list|(
literal|"_value"
argument_list|,
name|specialValue
argument_list|)
expr_stmt|;
comment|// noop: _value is special for aggregations, and is handled in ExpressionScriptBindings
comment|// TODO: if some uses it in a scoring expression, they will get a nasty failure when evaluating...need a
comment|// way to know this is for aggregations and so _value is ok to have...
block|}
elseif|else
if|if
condition|(
name|vars
operator|!=
literal|null
operator|&&
name|vars
operator|.
name|containsKey
argument_list|(
name|variable
argument_list|)
condition|)
block|{
comment|// TODO: document and/or error if vars contains _score?
comment|// NOTE: by checking for the variable in vars first, it allows masking document fields with a global constant,
comment|// but if we were to reverse it, we could provide a way to supply dynamic defaults for documents missing the field?
name|Object
name|value
init|=
name|vars
operator|.
name|get
argument_list|(
name|variable
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|instanceof
name|Number
condition|)
block|{
name|bindings
operator|.
name|add
argument_list|(
name|variable
argument_list|,
operator|new
name|DoubleConstValueSource
argument_list|(
operator|(
operator|(
name|Number
operator|)
name|value
operator|)
operator|.
name|doubleValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|ParseException
argument_list|(
literal|"Parameter ["
operator|+
name|variable
operator|+
literal|"] must be a numeric type"
argument_list|,
literal|0
argument_list|)
throw|;
block|}
block|}
else|else
block|{
name|String
name|fieldname
init|=
literal|null
decl_stmt|;
name|String
name|methodname
init|=
literal|null
decl_stmt|;
name|String
name|variablename
init|=
literal|"value"
decl_stmt|;
comment|// .value is the default for doc['field'], its optional.
name|boolean
name|dateAccessor
init|=
literal|false
decl_stmt|;
comment|// true if the variable is of type doc['field'].date.xxx
name|VariableContext
index|[]
name|parts
init|=
name|VariableContext
operator|.
name|parse
argument_list|(
name|variable
argument_list|)
decl_stmt|;
if|if
condition|(
name|parts
index|[
literal|0
index|]
operator|.
name|text
operator|.
name|equals
argument_list|(
literal|"doc"
argument_list|)
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|ParseException
argument_list|(
literal|"Unknown variable ["
operator|+
name|parts
index|[
literal|0
index|]
operator|.
name|text
operator|+
literal|"]"
argument_list|,
literal|0
argument_list|)
throw|;
block|}
if|if
condition|(
name|parts
operator|.
name|length
operator|<
literal|2
operator|||
name|parts
index|[
literal|1
index|]
operator|.
name|type
operator|!=
name|VariableContext
operator|.
name|Type
operator|.
name|STR_INDEX
condition|)
block|{
throw|throw
operator|new
name|ParseException
argument_list|(
literal|"Variable 'doc' must be used with a specific field like: doc['myfield']"
argument_list|,
literal|3
argument_list|)
throw|;
block|}
else|else
block|{
name|fieldname
operator|=
name|parts
index|[
literal|1
index|]
operator|.
name|text
expr_stmt|;
block|}
if|if
condition|(
name|parts
operator|.
name|length
operator|==
literal|3
condition|)
block|{
if|if
condition|(
name|parts
index|[
literal|2
index|]
operator|.
name|type
operator|==
name|VariableContext
operator|.
name|Type
operator|.
name|METHOD
condition|)
block|{
name|methodname
operator|=
name|parts
index|[
literal|2
index|]
operator|.
name|text
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parts
index|[
literal|2
index|]
operator|.
name|type
operator|==
name|VariableContext
operator|.
name|Type
operator|.
name|MEMBER
condition|)
block|{
name|variablename
operator|=
name|parts
index|[
literal|2
index|]
operator|.
name|text
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Only member variables or member methods may be accessed on a field when not accessing the field directly"
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
name|parts
operator|.
name|length
operator|>
literal|3
condition|)
block|{
comment|// access to the .date "object" within the field
if|if
condition|(
name|parts
operator|.
name|length
operator|==
literal|4
operator|&&
operator|(
literal|"date"
operator|.
name|equals
argument_list|(
name|parts
index|[
literal|2
index|]
operator|.
name|text
argument_list|)
operator|||
literal|"getDate"
operator|.
name|equals
argument_list|(
name|parts
index|[
literal|2
index|]
operator|.
name|text
argument_list|)
operator|)
condition|)
block|{
if|if
condition|(
name|parts
index|[
literal|3
index|]
operator|.
name|type
operator|==
name|VariableContext
operator|.
name|Type
operator|.
name|METHOD
condition|)
block|{
name|methodname
operator|=
name|parts
index|[
literal|3
index|]
operator|.
name|text
expr_stmt|;
name|dateAccessor
operator|=
literal|true
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|parts
index|[
literal|3
index|]
operator|.
name|type
operator|==
name|VariableContext
operator|.
name|Type
operator|.
name|MEMBER
condition|)
block|{
name|variablename
operator|=
name|parts
index|[
literal|3
index|]
operator|.
name|text
expr_stmt|;
name|dateAccessor
operator|=
literal|true
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|dateAccessor
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Variable ["
operator|+
name|variable
operator|+
literal|"] does not follow an allowed format of either doc['field'] or doc['field'].method()"
argument_list|)
throw|;
block|}
block|}
name|MappedFieldType
name|fieldType
init|=
name|mapper
operator|.
name|fullName
argument_list|(
name|fieldname
argument_list|)
decl_stmt|;
if|if
condition|(
name|fieldType
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ParseException
argument_list|(
literal|"Field ["
operator|+
name|fieldname
operator|+
literal|"] does not exist in mappings"
argument_list|,
literal|5
argument_list|)
throw|;
block|}
name|IndexFieldData
argument_list|<
name|?
argument_list|>
name|fieldData
init|=
name|lookup
operator|.
name|doc
argument_list|()
operator|.
name|fieldDataService
argument_list|()
operator|.
name|getForField
argument_list|(
name|fieldType
argument_list|)
decl_stmt|;
comment|// delegate valuesource creation based on field's type
comment|// there are three types of "fields" to expressions, and each one has a different "api" of variables and methods.
specifier|final
name|ValueSource
name|valueSource
decl_stmt|;
if|if
condition|(
name|fieldType
operator|instanceof
name|BaseGeoPointFieldMapper
operator|.
name|GeoPointFieldType
condition|)
block|{
comment|// geo
if|if
condition|(
name|methodname
operator|==
literal|null
condition|)
block|{
name|valueSource
operator|=
name|GeoField
operator|.
name|getVariable
argument_list|(
name|fieldData
argument_list|,
name|fieldname
argument_list|,
name|variablename
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|valueSource
operator|=
name|GeoField
operator|.
name|getMethod
argument_list|(
name|fieldData
argument_list|,
name|fieldname
argument_list|,
name|methodname
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|fieldType
operator|instanceof
name|LegacyDateFieldMapper
operator|.
name|DateFieldType
operator|||
name|fieldType
operator|instanceof
name|DateFieldMapper
operator|.
name|DateFieldType
condition|)
block|{
if|if
condition|(
name|dateAccessor
condition|)
block|{
comment|// date object
if|if
condition|(
name|methodname
operator|==
literal|null
condition|)
block|{
name|valueSource
operator|=
name|DateObject
operator|.
name|getVariable
argument_list|(
name|fieldData
argument_list|,
name|fieldname
argument_list|,
name|variablename
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|valueSource
operator|=
name|DateObject
operator|.
name|getMethod
argument_list|(
name|fieldData
argument_list|,
name|fieldname
argument_list|,
name|methodname
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
comment|// date field itself
if|if
condition|(
name|methodname
operator|==
literal|null
condition|)
block|{
name|valueSource
operator|=
name|DateField
operator|.
name|getVariable
argument_list|(
name|fieldData
argument_list|,
name|fieldname
argument_list|,
name|variablename
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|valueSource
operator|=
name|DateField
operator|.
name|getMethod
argument_list|(
name|fieldData
argument_list|,
name|fieldname
argument_list|,
name|methodname
argument_list|)
expr_stmt|;
block|}
block|}
block|}
elseif|else
if|if
condition|(
name|fieldData
operator|instanceof
name|IndexNumericFieldData
condition|)
block|{
comment|// number
if|if
condition|(
name|methodname
operator|==
literal|null
condition|)
block|{
name|valueSource
operator|=
name|NumericField
operator|.
name|getVariable
argument_list|(
name|fieldData
argument_list|,
name|fieldname
argument_list|,
name|variablename
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|valueSource
operator|=
name|NumericField
operator|.
name|getMethod
argument_list|(
name|fieldData
argument_list|,
name|fieldname
argument_list|,
name|methodname
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|ParseException
argument_list|(
literal|"Field ["
operator|+
name|fieldname
operator|+
literal|"] must be numeric, date, or geopoint"
argument_list|,
literal|5
argument_list|)
throw|;
block|}
name|bindings
operator|.
name|add
argument_list|(
name|variable
argument_list|,
name|valueSource
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// we defer "binding" of variables until here: give context for that variable
throw|throw
name|convertToScriptException
argument_list|(
literal|"link error"
argument_list|,
name|expr
operator|.
name|sourceText
argument_list|,
name|variable
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|final
name|boolean
name|needsScores
init|=
name|expr
operator|.
name|getSortField
argument_list|(
name|bindings
argument_list|,
literal|false
argument_list|)
operator|.
name|needsScores
argument_list|()
decl_stmt|;
return|return
operator|new
name|ExpressionSearchScript
argument_list|(
name|compiledScript
argument_list|,
name|bindings
argument_list|,
name|specialValue
argument_list|,
name|needsScores
argument_list|)
return|;
block|}
comment|/**      * converts a ParseException at compile-time or link-time to a ScriptException      */
DECL|method|convertToScriptException
specifier|private
name|ScriptException
name|convertToScriptException
parameter_list|(
name|String
name|message
parameter_list|,
name|String
name|source
parameter_list|,
name|String
name|portion
parameter_list|,
name|Throwable
name|cause
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|stack
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|stack
operator|.
name|add
argument_list|(
name|portion
argument_list|)
expr_stmt|;
name|StringBuilder
name|pointer
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|cause
operator|instanceof
name|ParseException
condition|)
block|{
name|int
name|offset
init|=
operator|(
operator|(
name|ParseException
operator|)
name|cause
operator|)
operator|.
name|getErrorOffset
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
name|offset
condition|;
name|i
operator|++
control|)
block|{
name|pointer
operator|.
name|append
argument_list|(
literal|' '
argument_list|)
expr_stmt|;
block|}
block|}
name|pointer
operator|.
name|append
argument_list|(
literal|"^---- HERE"
argument_list|)
expr_stmt|;
name|stack
operator|.
name|add
argument_list|(
name|pointer
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|ScriptException
argument_list|(
name|message
argument_list|,
name|cause
argument_list|,
name|stack
argument_list|,
name|source
argument_list|,
name|NAME
argument_list|)
throw|;
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
name|ExpressionExecutableScript
argument_list|(
name|compiledScript
argument_list|,
name|vars
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
block|{}
annotation|@
name|Override
DECL|method|scriptRemoved
specifier|public
name|void
name|scriptRemoved
parameter_list|(
name|CompiledScript
name|script
parameter_list|)
block|{
comment|// Nothing to do
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
block|}
end_class

end_unit

