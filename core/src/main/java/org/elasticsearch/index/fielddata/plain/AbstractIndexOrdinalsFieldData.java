begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.fielddata.plain
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|fielddata
operator|.
name|plain
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
name|DirectoryReader
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
name|index
operator|.
name|FilteredTermsEnum
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
name|index
operator|.
name|LeafReader
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
name|index
operator|.
name|LeafReaderContext
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
name|index
operator|.
name|Terms
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
name|index
operator|.
name|TermsEnum
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
name|BytesRef
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchException
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
name|IndexSettings
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
name|AtomicOrdinalsFieldData
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
name|IndexFieldDataCache
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
name|IndexOrdinalsFieldData
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
name|ordinals
operator|.
name|GlobalOrdinalsBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|breaker
operator|.
name|CircuitBreakerService
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_class
DECL|class|AbstractIndexOrdinalsFieldData
specifier|public
specifier|abstract
class|class
name|AbstractIndexOrdinalsFieldData
extends|extends
name|AbstractIndexFieldData
argument_list|<
name|AtomicOrdinalsFieldData
argument_list|>
implements|implements
name|IndexOrdinalsFieldData
block|{
DECL|field|minFrequency
DECL|field|maxFrequency
specifier|private
specifier|final
name|double
name|minFrequency
decl_stmt|,
name|maxFrequency
decl_stmt|;
DECL|field|minSegmentSize
specifier|private
specifier|final
name|int
name|minSegmentSize
decl_stmt|;
DECL|field|breakerService
specifier|protected
specifier|final
name|CircuitBreakerService
name|breakerService
decl_stmt|;
DECL|method|AbstractIndexOrdinalsFieldData
specifier|protected
name|AbstractIndexOrdinalsFieldData
parameter_list|(
name|IndexSettings
name|indexSettings
parameter_list|,
name|String
name|fieldName
parameter_list|,
name|IndexFieldDataCache
name|cache
parameter_list|,
name|CircuitBreakerService
name|breakerService
parameter_list|,
name|double
name|minFrequency
parameter_list|,
name|double
name|maxFrequency
parameter_list|,
name|int
name|minSegmentSize
parameter_list|)
block|{
name|super
argument_list|(
name|indexSettings
argument_list|,
name|fieldName
argument_list|,
name|cache
argument_list|)
expr_stmt|;
name|this
operator|.
name|breakerService
operator|=
name|breakerService
expr_stmt|;
name|this
operator|.
name|minFrequency
operator|=
name|minFrequency
expr_stmt|;
name|this
operator|.
name|maxFrequency
operator|=
name|maxFrequency
expr_stmt|;
name|this
operator|.
name|minSegmentSize
operator|=
name|minSegmentSize
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|loadGlobal
specifier|public
name|IndexOrdinalsFieldData
name|loadGlobal
parameter_list|(
name|DirectoryReader
name|indexReader
parameter_list|)
block|{
if|if
condition|(
name|indexReader
operator|.
name|leaves
argument_list|()
operator|.
name|size
argument_list|()
operator|<=
literal|1
condition|)
block|{
comment|// ordinals are already global
return|return
name|this
return|;
block|}
name|boolean
name|fieldFound
init|=
literal|false
decl_stmt|;
for|for
control|(
name|LeafReaderContext
name|context
range|:
name|indexReader
operator|.
name|leaves
argument_list|()
control|)
block|{
if|if
condition|(
name|context
operator|.
name|reader
argument_list|()
operator|.
name|getFieldInfos
argument_list|()
operator|.
name|fieldInfo
argument_list|(
name|getFieldName
argument_list|()
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|fieldFound
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
name|fieldFound
operator|==
literal|false
condition|)
block|{
comment|// Some directory readers may be wrapped and report different set of fields and use the same cache key.
comment|// If a field can't be found then it doesn't mean it isn't there,
comment|// so if a field doesn't exist then we don't cache it and just return an empty field data instance.
comment|// The next time the field is found, we do cache.
try|try
block|{
return|return
name|GlobalOrdinalsBuilder
operator|.
name|buildEmpty
argument_list|(
name|indexSettings
argument_list|,
name|indexReader
argument_list|,
name|this
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
try|try
block|{
return|return
name|cache
operator|.
name|load
argument_list|(
name|indexReader
argument_list|,
name|this
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|instanceof
name|ElasticsearchException
condition|)
block|{
throw|throw
operator|(
name|ElasticsearchException
operator|)
name|e
throw|;
block|}
else|else
block|{
throw|throw
operator|new
name|ElasticsearchException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|localGlobalDirect
specifier|public
name|IndexOrdinalsFieldData
name|localGlobalDirect
parameter_list|(
name|DirectoryReader
name|indexReader
parameter_list|)
throws|throws
name|Exception
block|{
return|return
name|GlobalOrdinalsBuilder
operator|.
name|build
argument_list|(
name|indexReader
argument_list|,
name|this
argument_list|,
name|indexSettings
argument_list|,
name|breakerService
argument_list|,
name|logger
argument_list|,
name|AbstractAtomicOrdinalsFieldData
operator|.
name|DEFAULT_SCRIPT_FUNCTION
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|empty
specifier|protected
name|AtomicOrdinalsFieldData
name|empty
parameter_list|(
name|int
name|maxDoc
parameter_list|)
block|{
return|return
name|AbstractAtomicOrdinalsFieldData
operator|.
name|empty
argument_list|()
return|;
block|}
DECL|method|filter
specifier|protected
name|TermsEnum
name|filter
parameter_list|(
name|Terms
name|terms
parameter_list|,
name|TermsEnum
name|iterator
parameter_list|,
name|LeafReader
name|reader
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|iterator
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|int
name|docCount
init|=
name|terms
operator|.
name|getDocCount
argument_list|()
decl_stmt|;
if|if
condition|(
name|docCount
operator|==
operator|-
literal|1
condition|)
block|{
name|docCount
operator|=
name|reader
operator|.
name|maxDoc
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|docCount
operator|>=
name|minSegmentSize
condition|)
block|{
specifier|final
name|int
name|minFreq
init|=
name|minFrequency
operator|>
literal|1.0
condition|?
operator|(
name|int
operator|)
name|minFrequency
else|:
call|(
name|int
call|)
argument_list|(
name|docCount
operator|*
name|minFrequency
argument_list|)
decl_stmt|;
specifier|final
name|int
name|maxFreq
init|=
name|maxFrequency
operator|>
literal|1.0
condition|?
operator|(
name|int
operator|)
name|maxFrequency
else|:
call|(
name|int
call|)
argument_list|(
name|docCount
operator|*
name|maxFrequency
argument_list|)
decl_stmt|;
if|if
condition|(
name|minFreq
operator|>
literal|1
operator|||
name|maxFreq
operator|<
name|docCount
condition|)
block|{
name|iterator
operator|=
operator|new
name|FrequencyFilter
argument_list|(
name|iterator
argument_list|,
name|minFreq
argument_list|,
name|maxFreq
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|iterator
return|;
block|}
DECL|class|FrequencyFilter
specifier|private
specifier|static
specifier|final
class|class
name|FrequencyFilter
extends|extends
name|FilteredTermsEnum
block|{
DECL|field|minFreq
specifier|private
name|int
name|minFreq
decl_stmt|;
DECL|field|maxFreq
specifier|private
name|int
name|maxFreq
decl_stmt|;
DECL|method|FrequencyFilter
name|FrequencyFilter
parameter_list|(
name|TermsEnum
name|delegate
parameter_list|,
name|int
name|minFreq
parameter_list|,
name|int
name|maxFreq
parameter_list|)
block|{
name|super
argument_list|(
name|delegate
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|this
operator|.
name|minFreq
operator|=
name|minFreq
expr_stmt|;
name|this
operator|.
name|maxFreq
operator|=
name|maxFreq
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|accept
specifier|protected
name|AcceptStatus
name|accept
parameter_list|(
name|BytesRef
name|arg0
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|docFreq
init|=
name|docFreq
argument_list|()
decl_stmt|;
if|if
condition|(
name|docFreq
operator|>=
name|minFreq
operator|&&
name|docFreq
operator|<=
name|maxFreq
condition|)
block|{
return|return
name|AcceptStatus
operator|.
name|YES
return|;
block|}
return|return
name|AcceptStatus
operator|.
name|NO
return|;
block|}
block|}
block|}
end_class

end_unit

