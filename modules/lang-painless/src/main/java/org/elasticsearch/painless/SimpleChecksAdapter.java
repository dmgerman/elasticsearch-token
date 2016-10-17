begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.painless
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|painless
package|;
end_package

begin_import
import|import
name|org
operator|.
name|objectweb
operator|.
name|asm
operator|.
name|ClassVisitor
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
name|objectweb
operator|.
name|asm
operator|.
name|MethodVisitor
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
name|Opcodes
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
name|util
operator|.
name|CheckClassAdapter
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
name|util
operator|.
name|CheckMethodAdapter
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

begin_comment
comment|/**   * A CheckClassAdapter that does not use setAccessible to try to access private fields of Label!  *<p>  * This means jump insns are not checked, but we still get all the other checking.  */
end_comment

begin_comment
comment|// TODO: we should really try to get this fixed in ASM!
end_comment

begin_class
DECL|class|SimpleChecksAdapter
specifier|public
class|class
name|SimpleChecksAdapter
extends|extends
name|CheckClassAdapter
block|{
DECL|method|SimpleChecksAdapter
specifier|public
name|SimpleChecksAdapter
parameter_list|(
name|ClassVisitor
name|cv
parameter_list|)
block|{
name|super
argument_list|(
name|WriterConstants
operator|.
name|ASM_VERSION
argument_list|,
name|cv
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|visitMethod
specifier|public
name|MethodVisitor
name|visitMethod
parameter_list|(
name|int
name|access
parameter_list|,
name|String
name|name
parameter_list|,
name|String
name|desc
parameter_list|,
name|String
name|signature
parameter_list|,
name|String
index|[]
name|exceptions
parameter_list|)
block|{
name|MethodVisitor
name|in
init|=
name|cv
operator|.
name|visitMethod
argument_list|(
name|access
argument_list|,
name|name
argument_list|,
name|desc
argument_list|,
name|signature
argument_list|,
name|exceptions
argument_list|)
decl_stmt|;
name|CheckMethodAdapter
name|checker
init|=
operator|new
name|CheckMethodAdapter
argument_list|(
name|WriterConstants
operator|.
name|ASM_VERSION
argument_list|,
name|in
argument_list|,
operator|new
name|HashMap
argument_list|<
name|Label
argument_list|,
name|Integer
argument_list|>
argument_list|()
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|void
name|visitJumpInsn
parameter_list|(
name|int
name|opcode
parameter_list|,
name|Label
name|label
parameter_list|)
block|{
name|mv
operator|.
name|visitJumpInsn
argument_list|(
name|opcode
argument_list|,
name|label
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|visitTryCatchBlock
parameter_list|(
name|Label
name|start
parameter_list|,
name|Label
name|end
parameter_list|,
name|Label
name|handler
parameter_list|,
name|String
name|type
parameter_list|)
block|{
name|mv
operator|.
name|visitTryCatchBlock
argument_list|(
name|start
argument_list|,
name|end
argument_list|,
name|handler
argument_list|,
name|type
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
name|checker
operator|.
name|version
operator|=
name|WriterConstants
operator|.
name|CLASS_VERSION
expr_stmt|;
return|return
name|checker
return|;
block|}
block|}
end_class

end_unit
