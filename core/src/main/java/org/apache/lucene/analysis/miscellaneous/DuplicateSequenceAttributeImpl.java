begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.apache.lucene.analysis.miscellaneous
package|package
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|analysis
operator|.
name|miscellaneous
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
name|util
operator|.
name|AttributeImpl
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
name|util
operator|.
name|AttributeReflector
import|;
end_import

begin_class
DECL|class|DuplicateSequenceAttributeImpl
specifier|public
class|class
name|DuplicateSequenceAttributeImpl
extends|extends
name|AttributeImpl
implements|implements
name|DuplicateSequenceAttribute
block|{
DECL|field|numPriorUsesInASequence
specifier|protected
name|short
name|numPriorUsesInASequence
init|=
literal|0
decl_stmt|;
annotation|@
name|Override
DECL|method|clear
specifier|public
name|void
name|clear
parameter_list|()
block|{
name|numPriorUsesInASequence
operator|=
literal|0
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|copyTo
specifier|public
name|void
name|copyTo
parameter_list|(
name|AttributeImpl
name|target
parameter_list|)
block|{
name|DuplicateSequenceAttributeImpl
name|t
init|=
operator|(
name|DuplicateSequenceAttributeImpl
operator|)
name|target
decl_stmt|;
name|t
operator|.
name|numPriorUsesInASequence
operator|=
name|numPriorUsesInASequence
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getNumPriorUsesInASequence
specifier|public
name|short
name|getNumPriorUsesInASequence
parameter_list|()
block|{
return|return
name|numPriorUsesInASequence
return|;
block|}
annotation|@
name|Override
DECL|method|setNumPriorUsesInASequence
specifier|public
name|void
name|setNumPriorUsesInASequence
parameter_list|(
name|short
name|len
parameter_list|)
block|{
name|numPriorUsesInASequence
operator|=
name|len
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|reflectWith
specifier|public
name|void
name|reflectWith
parameter_list|(
name|AttributeReflector
name|reflector
parameter_list|)
block|{
name|reflector
operator|.
name|reflect
argument_list|(
name|DuplicateSequenceAttribute
operator|.
name|class
argument_list|,
literal|"sequenceLength"
argument_list|,
name|numPriorUsesInASequence
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

