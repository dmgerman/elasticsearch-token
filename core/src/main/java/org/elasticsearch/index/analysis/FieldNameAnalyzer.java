begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.analysis
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|analysis
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
name|common
operator|.
name|collect
operator|.
name|CopyOnWriteHashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
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

begin_class
DECL|class|FieldNameAnalyzer
specifier|public
specifier|final
class|class
name|FieldNameAnalyzer
extends|extends
name|DelegatingAnalyzerWrapper
block|{
DECL|field|analyzers
specifier|private
specifier|final
name|CopyOnWriteHashMap
argument_list|<
name|String
argument_list|,
name|Analyzer
argument_list|>
name|analyzers
decl_stmt|;
DECL|field|defaultAnalyzer
specifier|private
specifier|final
name|Analyzer
name|defaultAnalyzer
decl_stmt|;
DECL|method|FieldNameAnalyzer
specifier|public
name|FieldNameAnalyzer
parameter_list|(
name|Analyzer
name|defaultAnalyzer
parameter_list|)
block|{
name|this
argument_list|(
operator|new
name|CopyOnWriteHashMap
argument_list|<
name|String
argument_list|,
name|Analyzer
argument_list|>
argument_list|()
argument_list|,
name|defaultAnalyzer
argument_list|)
expr_stmt|;
block|}
DECL|method|FieldNameAnalyzer
specifier|public
name|FieldNameAnalyzer
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Analyzer
argument_list|>
name|analyzers
parameter_list|,
name|Analyzer
name|defaultAnalyzer
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
name|analyzers
operator|=
name|CopyOnWriteHashMap
operator|.
name|copyOf
argument_list|(
name|analyzers
argument_list|)
expr_stmt|;
name|this
operator|.
name|defaultAnalyzer
operator|=
name|defaultAnalyzer
expr_stmt|;
block|}
DECL|method|analyzers
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Analyzer
argument_list|>
name|analyzers
parameter_list|()
block|{
return|return
name|analyzers
return|;
block|}
DECL|method|defaultAnalyzer
specifier|public
name|Analyzer
name|defaultAnalyzer
parameter_list|()
block|{
return|return
name|defaultAnalyzer
return|;
block|}
comment|/** NOTE: public so MapperAnalyzer can invoke: */
annotation|@
name|Override
DECL|method|getWrappedAnalyzer
specifier|public
name|Analyzer
name|getWrappedAnalyzer
parameter_list|(
name|String
name|fieldName
parameter_list|)
block|{
return|return
name|getAnalyzer
argument_list|(
name|fieldName
argument_list|)
return|;
block|}
DECL|method|getAnalyzer
specifier|private
name|Analyzer
name|getAnalyzer
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|Analyzer
name|analyzer
init|=
name|analyzers
operator|.
name|get
argument_list|(
name|name
argument_list|)
decl_stmt|;
if|if
condition|(
name|analyzer
operator|!=
literal|null
condition|)
block|{
return|return
name|analyzer
return|;
block|}
comment|// Don't be lenient here and return the default analyzer
comment|// Fields need to be explicitly added
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Field ["
operator|+
name|name
operator|+
literal|"] has no associated analyzer"
argument_list|)
throw|;
block|}
comment|/**      * Return a new instance that contains the union of this and of the provided analyzers.      */
DECL|method|copyAndAddAll
specifier|public
name|FieldNameAnalyzer
name|copyAndAddAll
parameter_list|(
name|Collection
argument_list|<
name|?
extends|extends
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Analyzer
argument_list|>
argument_list|>
name|mappers
parameter_list|)
block|{
name|CopyOnWriteHashMap
argument_list|<
name|String
argument_list|,
name|Analyzer
argument_list|>
name|analyzers
init|=
name|this
operator|.
name|analyzers
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Analyzer
argument_list|>
name|entry
range|:
name|mappers
control|)
block|{
name|Analyzer
name|analyzer
init|=
name|entry
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|analyzer
operator|==
literal|null
condition|)
block|{
name|analyzer
operator|=
name|defaultAnalyzer
expr_stmt|;
block|}
name|analyzers
operator|=
name|analyzers
operator|.
name|copyAndPut
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|analyzer
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|FieldNameAnalyzer
argument_list|(
name|analyzers
argument_list|,
name|defaultAnalyzer
argument_list|)
return|;
block|}
block|}
end_class

end_unit
