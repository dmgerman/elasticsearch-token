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
name|MethodWriter
import|;
end_import

begin_comment
comment|/**  * Represents a throw statement.  */
end_comment

begin_class
DECL|class|SThrow
specifier|public
specifier|final
class|class
name|SThrow
extends|extends
name|AStatement
block|{
DECL|field|expression
name|AExpression
name|expression
decl_stmt|;
DECL|method|SThrow
specifier|public
name|SThrow
parameter_list|(
name|int
name|line
parameter_list|,
name|int
name|offset
parameter_list|,
name|String
name|location
parameter_list|,
name|AExpression
name|expression
parameter_list|)
block|{
name|super
argument_list|(
name|line
argument_list|,
name|offset
argument_list|,
name|location
argument_list|)
expr_stmt|;
name|this
operator|.
name|expression
operator|=
name|expression
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
name|expression
operator|.
name|expected
operator|=
name|Definition
operator|.
name|EXCEPTION_TYPE
expr_stmt|;
name|expression
operator|.
name|analyze
argument_list|(
name|variables
argument_list|)
expr_stmt|;
name|expression
operator|=
name|expression
operator|.
name|cast
argument_list|(
name|variables
argument_list|)
expr_stmt|;
name|methodEscape
operator|=
literal|true
expr_stmt|;
name|loopEscape
operator|=
literal|true
expr_stmt|;
name|allEscape
operator|=
literal|true
expr_stmt|;
name|statementCount
operator|=
literal|1
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
name|writeDebugInfo
argument_list|(
name|writer
argument_list|)
expr_stmt|;
name|expression
operator|.
name|write
argument_list|(
name|writer
argument_list|)
expr_stmt|;
name|writer
operator|.
name|throwException
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

