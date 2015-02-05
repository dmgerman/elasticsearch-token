begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
package|;
end_package

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
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|analysis
operator|.
name|Analyzer
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
name|analysis
operator|.
name|DelegatingAnalyzerWrapper
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
name|analysis
operator|.
name|FieldNameAnalyzer
import|;
end_import

begin_comment
comment|/** Hacky analyzer to dispatch per-thread based on the type of the current document being indexed, to look up the per-field Analyzer.  Once  *  mappings are moved to the index level we can remove this. */
end_comment

begin_class
DECL|class|MapperAnalyzer
specifier|public
class|class
name|MapperAnalyzer
extends|extends
name|DelegatingAnalyzerWrapper
block|{
DECL|field|mapperService
specifier|private
specifier|final
name|MapperService
name|mapperService
decl_stmt|;
comment|/** Which type this thread is currently indexing. */
DECL|field|threadTypes
specifier|private
specifier|final
name|ThreadLocal
argument_list|<
name|String
argument_list|>
name|threadTypes
init|=
operator|new
name|ThreadLocal
argument_list|<>
argument_list|()
decl_stmt|;
DECL|method|MapperAnalyzer
specifier|public
name|MapperAnalyzer
parameter_list|(
name|MapperService
name|mapperService
parameter_list|)
block|{
name|super
argument_list|(
name|Analyzer
operator|.
name|PER_FIELD_REUSE_STRATEGY
argument_list|)
expr_stmt|;
name|this
operator|.
name|mapperService
operator|=
name|mapperService
expr_stmt|;
block|}
comment|/** Any thread that is about to use this analyzer for indexing must first set the type by calling this. */
DECL|method|setType
specifier|public
name|void
name|setType
parameter_list|(
name|String
name|type
parameter_list|)
block|{
name|threadTypes
operator|.
name|set
argument_list|(
name|type
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getWrappedAnalyzer
specifier|protected
name|Analyzer
name|getWrappedAnalyzer
parameter_list|(
name|String
name|fieldName
parameter_list|)
block|{
comment|// First get the FieldNameAnalyzer from the type, then ask it for the right analyzer for this field, or the default index analyzer:
return|return
operator|(
operator|(
name|FieldNameAnalyzer
operator|)
name|mapperService
operator|.
name|documentMapper
argument_list|(
name|threadTypes
operator|.
name|get
argument_list|()
argument_list|)
operator|.
name|mappers
argument_list|()
operator|.
name|indexAnalyzer
argument_list|()
operator|)
operator|.
name|getWrappedAnalyzer
argument_list|(
name|fieldName
argument_list|)
return|;
block|}
block|}
end_class

end_unit
