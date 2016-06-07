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
name|Variables
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

begin_comment
comment|/**  * Represents a series of declarations.  */
end_comment

begin_class
DECL|class|SDeclBlock
specifier|public
specifier|final
class|class
name|SDeclBlock
extends|extends
name|AStatement
block|{
DECL|field|declarations
specifier|final
name|List
argument_list|<
name|SDeclaration
argument_list|>
name|declarations
decl_stmt|;
DECL|method|SDeclBlock
specifier|public
name|SDeclBlock
parameter_list|(
name|Location
name|location
parameter_list|,
name|List
argument_list|<
name|SDeclaration
argument_list|>
name|declarations
parameter_list|)
block|{
name|super
argument_list|(
name|location
argument_list|)
expr_stmt|;
name|this
operator|.
name|declarations
operator|=
name|Collections
operator|.
name|unmodifiableList
argument_list|(
name|declarations
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|analyze
name|void
name|analyze
parameter_list|(
name|Variables
name|variables
parameter_list|)
block|{
for|for
control|(
name|SDeclaration
name|declaration
range|:
name|declarations
control|)
block|{
name|declaration
operator|.
name|analyze
argument_list|(
name|variables
argument_list|)
expr_stmt|;
block|}
name|statementCount
operator|=
name|declarations
operator|.
name|size
argument_list|()
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
parameter_list|)
block|{
for|for
control|(
name|SDeclaration
name|declaration
range|:
name|declarations
control|)
block|{
name|declaration
operator|.
name|write
argument_list|(
name|writer
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit
