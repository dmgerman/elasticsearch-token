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
name|Locals
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
name|Locals
operator|.
name|Variable
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
name|Location
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
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|painless
operator|.
name|node
operator|.
name|SFunction
operator|.
name|FunctionReserved
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
name|Globals
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
name|Type
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
name|Collections
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

begin_class
DECL|class|ELambda
specifier|public
class|class
name|ELambda
extends|extends
name|AExpression
implements|implements
name|ILambda
block|{
DECL|field|name
specifier|final
name|String
name|name
decl_stmt|;
DECL|field|reserved
specifier|final
name|FunctionReserved
name|reserved
decl_stmt|;
DECL|field|paramTypeStrs
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|paramTypeStrs
decl_stmt|;
DECL|field|paramNameStrs
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|paramNameStrs
decl_stmt|;
DECL|field|statements
specifier|final
name|List
argument_list|<
name|AStatement
argument_list|>
name|statements
decl_stmt|;
comment|// desugared synthetic method (lambda body)
DECL|field|desugared
name|SFunction
name|desugared
decl_stmt|;
comment|// method ref (impl detail)
DECL|field|impl
name|ILambda
name|impl
decl_stmt|;
DECL|method|ELambda
specifier|public
name|ELambda
parameter_list|(
name|String
name|name
parameter_list|,
name|FunctionReserved
name|reserved
parameter_list|,
name|Location
name|location
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|paramTypes
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|paramNames
parameter_list|,
name|List
argument_list|<
name|AStatement
argument_list|>
name|statements
parameter_list|)
block|{
name|super
argument_list|(
name|location
argument_list|)
expr_stmt|;
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
name|this
operator|.
name|reserved
operator|=
name|reserved
expr_stmt|;
name|this
operator|.
name|paramTypeStrs
operator|=
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|paramTypes
argument_list|)
expr_stmt|;
name|this
operator|.
name|paramNameStrs
operator|=
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|paramNames
argument_list|)
expr_stmt|;
name|this
operator|.
name|statements
operator|=
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|statements
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|analyze
name|void
name|analyze
parameter_list|(
name|Locals
name|locals
parameter_list|)
block|{
comment|// desugar lambda body into a synthetic method
name|desugared
operator|=
operator|new
name|SFunction
argument_list|(
name|reserved
argument_list|,
name|location
argument_list|,
literal|"def"
argument_list|,
name|name
argument_list|,
name|paramTypeStrs
argument_list|,
name|paramNameStrs
argument_list|,
name|statements
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|desugared
operator|.
name|generate
argument_list|()
expr_stmt|;
name|List
argument_list|<
name|Variable
argument_list|>
name|captures
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|desugared
operator|.
name|analyze
argument_list|(
name|Locals
operator|.
name|newLambdaScope
argument_list|(
name|locals
operator|.
name|getProgramScope
argument_list|()
argument_list|,
name|desugared
operator|.
name|parameters
argument_list|,
name|captures
argument_list|)
argument_list|)
expr_stmt|;
comment|// setup reference
name|EFunctionRef
name|ref
init|=
operator|new
name|EFunctionRef
argument_list|(
name|location
argument_list|,
literal|"this"
argument_list|,
name|name
argument_list|)
decl_stmt|;
name|ref
operator|.
name|expected
operator|=
name|expected
expr_stmt|;
comment|// hack, create a new scope, with our method, so the ref can see it (impl detail)
name|locals
operator|=
name|Locals
operator|.
name|newLocalScope
argument_list|(
name|locals
argument_list|)
expr_stmt|;
name|locals
operator|.
name|addMethod
argument_list|(
name|desugared
operator|.
name|method
argument_list|)
expr_stmt|;
name|ref
operator|.
name|analyze
argument_list|(
name|locals
argument_list|)
expr_stmt|;
name|actual
operator|=
name|ref
operator|.
name|actual
expr_stmt|;
name|impl
operator|=
name|ref
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|write
name|void
name|write
parameter_list|(
name|MethodWriter
name|writer
parameter_list|,
name|Globals
name|globals
parameter_list|)
block|{
name|AExpression
name|expr
init|=
operator|(
name|AExpression
operator|)
name|impl
decl_stmt|;
name|expr
operator|.
name|write
argument_list|(
name|writer
argument_list|,
name|globals
argument_list|)
expr_stmt|;
comment|// add synthetic method to the queue to be written
name|globals
operator|.
name|addSyntheticMethod
argument_list|(
name|desugared
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getPointer
specifier|public
name|String
name|getPointer
parameter_list|()
block|{
return|return
name|impl
operator|.
name|getPointer
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|getCaptures
specifier|public
name|Type
index|[]
name|getCaptures
parameter_list|()
block|{
return|return
name|impl
operator|.
name|getCaptures
argument_list|()
return|;
block|}
block|}
end_class

end_unit

