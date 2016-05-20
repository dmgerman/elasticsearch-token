begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.painless.node
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|painless
operator|.
name|node
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|painless
operator|.
name|CompilerSettings
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|painless
operator|.
name|Definition
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|painless
operator|.
name|Definition
operator|.
name|Sort
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|painless
operator|.
name|Definition
operator|.
name|Type
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|painless
operator|.
name|AnalyzerCaster
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|painless
operator|.
name|Operation
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|painless
operator|.
name|Variables
import|;
end_import

begin_import
import|import
name|org
operator|.
name|objectweb
operator|.
name|asm
operator|.
name|Label
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|painless
operator|.
name|MethodWriter
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|painless
operator|.
name|WriterConstants
operator|.
name|DEF_NEG_CALL
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|painless
operator|.
name|WriterConstants
operator|.
name|DEF_NOT_CALL
import|;
end_import

begin_comment
comment|/**  * Represents a unary math expression.  */
end_comment

begin_class
DECL|class|EUnary
specifier|public
specifier|final
class|class
name|EUnary
extends|extends
name|AExpression
block|{
DECL|field|operation
name|Operation
name|operation
decl_stmt|;
DECL|field|child
name|AExpression
name|child
decl_stmt|;
DECL|method|EUnary
specifier|public
name|EUnary
parameter_list|(
specifier|final
name|int
name|line
parameter_list|,
specifier|final
name|String
name|location
parameter_list|,
specifier|final
name|Operation
name|operation
parameter_list|,
specifier|final
name|AExpression
name|child
parameter_list|)
block|{
name|super
argument_list|(
name|line
argument_list|,
name|location
argument_list|)
expr_stmt|;
name|this
operator|.
name|operation
operator|=
name|operation
expr_stmt|;
name|this
operator|.
name|child
operator|=
name|child
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|analyze
name|void
name|analyze
parameter_list|(
specifier|final
name|CompilerSettings
name|settings
parameter_list|,
specifier|final
name|Variables
name|variables
parameter_list|)
block|{
if|if
condition|(
name|operation
operator|==
name|Operation
operator|.
name|NOT
condition|)
block|{
name|analyzeNot
argument_list|(
name|settings
argument_list|,
name|variables
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|operation
operator|==
name|Operation
operator|.
name|BWNOT
condition|)
block|{
name|analyzeBWNot
argument_list|(
name|settings
argument_list|,
name|variables
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|operation
operator|==
name|Operation
operator|.
name|ADD
condition|)
block|{
name|analyzerAdd
argument_list|(
name|settings
argument_list|,
name|variables
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|operation
operator|==
name|Operation
operator|.
name|SUB
condition|)
block|{
name|analyzerSub
argument_list|(
name|settings
argument_list|,
name|variables
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
name|error
argument_list|(
literal|"Illegal tree structure."
argument_list|)
argument_list|)
throw|;
block|}
block|}
DECL|method|analyzeNot
name|void
name|analyzeNot
parameter_list|(
specifier|final
name|CompilerSettings
name|settings
parameter_list|,
specifier|final
name|Variables
name|variables
parameter_list|)
block|{
name|child
operator|.
name|expected
operator|=
name|Definition
operator|.
name|booleanType
expr_stmt|;
name|child
operator|.
name|analyze
argument_list|(
name|settings
argument_list|,
name|variables
argument_list|)
expr_stmt|;
name|child
operator|=
name|child
operator|.
name|cast
argument_list|(
name|settings
argument_list|,
name|variables
argument_list|)
expr_stmt|;
if|if
condition|(
name|child
operator|.
name|constant
operator|!=
literal|null
condition|)
block|{
name|constant
operator|=
operator|!
operator|(
name|boolean
operator|)
name|child
operator|.
name|constant
expr_stmt|;
block|}
name|actual
operator|=
name|Definition
operator|.
name|booleanType
expr_stmt|;
block|}
DECL|method|analyzeBWNot
name|void
name|analyzeBWNot
parameter_list|(
specifier|final
name|CompilerSettings
name|settings
parameter_list|,
specifier|final
name|Variables
name|variables
parameter_list|)
block|{
name|child
operator|.
name|analyze
argument_list|(
name|settings
argument_list|,
name|variables
argument_list|)
expr_stmt|;
specifier|final
name|Type
name|promote
init|=
name|AnalyzerCaster
operator|.
name|promoteNumeric
argument_list|(
name|child
operator|.
name|actual
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
decl_stmt|;
if|if
condition|(
name|promote
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ClassCastException
argument_list|(
name|error
argument_list|(
literal|"Cannot apply not [~] to type ["
operator|+
name|child
operator|.
name|actual
operator|.
name|name
operator|+
literal|"]."
argument_list|)
argument_list|)
throw|;
block|}
name|child
operator|.
name|expected
operator|=
name|promote
expr_stmt|;
name|child
operator|=
name|child
operator|.
name|cast
argument_list|(
name|settings
argument_list|,
name|variables
argument_list|)
expr_stmt|;
if|if
condition|(
name|child
operator|.
name|constant
operator|!=
literal|null
condition|)
block|{
specifier|final
name|Sort
name|sort
init|=
name|promote
operator|.
name|sort
decl_stmt|;
if|if
condition|(
name|sort
operator|==
name|Sort
operator|.
name|INT
condition|)
block|{
name|constant
operator|=
operator|~
operator|(
name|int
operator|)
name|child
operator|.
name|constant
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|sort
operator|==
name|Sort
operator|.
name|LONG
condition|)
block|{
name|constant
operator|=
operator|~
operator|(
name|long
operator|)
name|child
operator|.
name|constant
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
name|error
argument_list|(
literal|"Illegal tree structure."
argument_list|)
argument_list|)
throw|;
block|}
block|}
name|actual
operator|=
name|promote
expr_stmt|;
block|}
DECL|method|analyzerAdd
name|void
name|analyzerAdd
parameter_list|(
specifier|final
name|CompilerSettings
name|settings
parameter_list|,
specifier|final
name|Variables
name|variables
parameter_list|)
block|{
name|child
operator|.
name|analyze
argument_list|(
name|settings
argument_list|,
name|variables
argument_list|)
expr_stmt|;
specifier|final
name|Type
name|promote
init|=
name|AnalyzerCaster
operator|.
name|promoteNumeric
argument_list|(
name|child
operator|.
name|actual
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
decl_stmt|;
if|if
condition|(
name|promote
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ClassCastException
argument_list|(
name|error
argument_list|(
literal|"Cannot apply positive [+] to type ["
operator|+
name|child
operator|.
name|actual
operator|.
name|name
operator|+
literal|"]."
argument_list|)
argument_list|)
throw|;
block|}
name|child
operator|.
name|expected
operator|=
name|promote
expr_stmt|;
name|child
operator|=
name|child
operator|.
name|cast
argument_list|(
name|settings
argument_list|,
name|variables
argument_list|)
expr_stmt|;
if|if
condition|(
name|child
operator|.
name|constant
operator|!=
literal|null
condition|)
block|{
specifier|final
name|Sort
name|sort
init|=
name|promote
operator|.
name|sort
decl_stmt|;
if|if
condition|(
name|sort
operator|==
name|Sort
operator|.
name|INT
condition|)
block|{
name|constant
operator|=
operator|+
operator|(
name|int
operator|)
name|child
operator|.
name|constant
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|sort
operator|==
name|Sort
operator|.
name|LONG
condition|)
block|{
name|constant
operator|=
operator|+
operator|(
name|long
operator|)
name|child
operator|.
name|constant
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|sort
operator|==
name|Sort
operator|.
name|FLOAT
condition|)
block|{
name|constant
operator|=
operator|+
operator|(
name|float
operator|)
name|child
operator|.
name|constant
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|sort
operator|==
name|Sort
operator|.
name|DOUBLE
condition|)
block|{
name|constant
operator|=
operator|+
operator|(
name|double
operator|)
name|child
operator|.
name|constant
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
name|error
argument_list|(
literal|"Illegal tree structure."
argument_list|)
argument_list|)
throw|;
block|}
block|}
name|actual
operator|=
name|promote
expr_stmt|;
block|}
DECL|method|analyzerSub
name|void
name|analyzerSub
parameter_list|(
specifier|final
name|CompilerSettings
name|settings
parameter_list|,
specifier|final
name|Variables
name|variables
parameter_list|)
block|{
name|child
operator|.
name|analyze
argument_list|(
name|settings
argument_list|,
name|variables
argument_list|)
expr_stmt|;
specifier|final
name|Type
name|promote
init|=
name|AnalyzerCaster
operator|.
name|promoteNumeric
argument_list|(
name|child
operator|.
name|actual
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
decl_stmt|;
if|if
condition|(
name|promote
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ClassCastException
argument_list|(
name|error
argument_list|(
literal|"Cannot apply negative [-] to type ["
operator|+
name|child
operator|.
name|actual
operator|.
name|name
operator|+
literal|"]."
argument_list|)
argument_list|)
throw|;
block|}
name|child
operator|.
name|expected
operator|=
name|promote
expr_stmt|;
name|child
operator|=
name|child
operator|.
name|cast
argument_list|(
name|settings
argument_list|,
name|variables
argument_list|)
expr_stmt|;
if|if
condition|(
name|child
operator|.
name|constant
operator|!=
literal|null
condition|)
block|{
specifier|final
name|Sort
name|sort
init|=
name|promote
operator|.
name|sort
decl_stmt|;
if|if
condition|(
name|sort
operator|==
name|Sort
operator|.
name|INT
condition|)
block|{
name|constant
operator|=
operator|-
operator|(
name|int
operator|)
name|child
operator|.
name|constant
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|sort
operator|==
name|Sort
operator|.
name|LONG
condition|)
block|{
name|constant
operator|=
operator|-
operator|(
name|long
operator|)
name|child
operator|.
name|constant
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|sort
operator|==
name|Sort
operator|.
name|FLOAT
condition|)
block|{
name|constant
operator|=
operator|-
operator|(
name|float
operator|)
name|child
operator|.
name|constant
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|sort
operator|==
name|Sort
operator|.
name|DOUBLE
condition|)
block|{
name|constant
operator|=
operator|-
operator|(
name|double
operator|)
name|child
operator|.
name|constant
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
name|error
argument_list|(
literal|"Illegal tree structure."
argument_list|)
argument_list|)
throw|;
block|}
block|}
name|actual
operator|=
name|promote
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|write
name|void
name|write
parameter_list|(
specifier|final
name|CompilerSettings
name|settings
parameter_list|,
specifier|final
name|MethodWriter
name|adapter
parameter_list|)
block|{
if|if
condition|(
name|operation
operator|==
name|Operation
operator|.
name|NOT
condition|)
block|{
if|if
condition|(
name|tru
operator|==
literal|null
operator|&&
name|fals
operator|==
literal|null
condition|)
block|{
specifier|final
name|Label
name|localfals
init|=
operator|new
name|Label
argument_list|()
decl_stmt|;
specifier|final
name|Label
name|end
init|=
operator|new
name|Label
argument_list|()
decl_stmt|;
name|child
operator|.
name|fals
operator|=
name|localfals
expr_stmt|;
name|child
operator|.
name|write
argument_list|(
name|settings
argument_list|,
name|adapter
argument_list|)
expr_stmt|;
name|adapter
operator|.
name|push
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|adapter
operator|.
name|goTo
argument_list|(
name|end
argument_list|)
expr_stmt|;
name|adapter
operator|.
name|mark
argument_list|(
name|localfals
argument_list|)
expr_stmt|;
name|adapter
operator|.
name|push
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|adapter
operator|.
name|mark
argument_list|(
name|end
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|child
operator|.
name|tru
operator|=
name|fals
expr_stmt|;
name|child
operator|.
name|fals
operator|=
name|tru
expr_stmt|;
name|child
operator|.
name|write
argument_list|(
name|settings
argument_list|,
name|adapter
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
specifier|final
name|org
operator|.
name|objectweb
operator|.
name|asm
operator|.
name|Type
name|type
init|=
name|actual
operator|.
name|type
decl_stmt|;
specifier|final
name|Sort
name|sort
init|=
name|actual
operator|.
name|sort
decl_stmt|;
name|child
operator|.
name|write
argument_list|(
name|settings
argument_list|,
name|adapter
argument_list|)
expr_stmt|;
if|if
condition|(
name|operation
operator|==
name|Operation
operator|.
name|BWNOT
condition|)
block|{
if|if
condition|(
name|sort
operator|==
name|Sort
operator|.
name|DEF
condition|)
block|{
name|adapter
operator|.
name|invokeStatic
argument_list|(
name|Definition
operator|.
name|defobjType
operator|.
name|type
argument_list|,
name|DEF_NOT_CALL
argument_list|)
expr_stmt|;
block|}
else|else
block|{
if|if
condition|(
name|sort
operator|==
name|Sort
operator|.
name|INT
condition|)
block|{
name|adapter
operator|.
name|push
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|sort
operator|==
name|Sort
operator|.
name|LONG
condition|)
block|{
name|adapter
operator|.
name|push
argument_list|(
operator|-
literal|1L
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
name|error
argument_list|(
literal|"Illegal tree structure."
argument_list|)
argument_list|)
throw|;
block|}
name|adapter
operator|.
name|math
argument_list|(
name|MethodWriter
operator|.
name|XOR
argument_list|,
name|type
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|operation
operator|==
name|Operation
operator|.
name|SUB
condition|)
block|{
if|if
condition|(
name|sort
operator|==
name|Sort
operator|.
name|DEF
condition|)
block|{
name|adapter
operator|.
name|invokeStatic
argument_list|(
name|Definition
operator|.
name|defobjType
operator|.
name|type
argument_list|,
name|DEF_NEG_CALL
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|adapter
operator|.
name|math
argument_list|(
name|MethodWriter
operator|.
name|NEG
argument_list|,
name|type
argument_list|)
expr_stmt|;
block|}
block|}
elseif|else
if|if
condition|(
name|operation
operator|!=
name|Operation
operator|.
name|ADD
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
name|error
argument_list|(
literal|"Illegal tree structure."
argument_list|)
argument_list|)
throw|;
block|}
name|adapter
operator|.
name|writeBranch
argument_list|(
name|tru
argument_list|,
name|fals
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

