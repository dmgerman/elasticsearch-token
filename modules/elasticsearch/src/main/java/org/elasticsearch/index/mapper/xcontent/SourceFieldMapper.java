begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper.xcontent
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|xcontent
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
name|document
operator|.
name|*
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchParseException
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
name|compress
operator|.
name|lzf
operator|.
name|LZFDecoder
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
name|compress
operator|.
name|lzf
operator|.
name|LZFEncoder
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
name|lucene
operator|.
name|Lucene
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
name|unit
operator|.
name|ByteSizeValue
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
name|xcontent
operator|.
name|XContentBuilder
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
name|MergeMappingException
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

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|SourceFieldMapper
specifier|public
class|class
name|SourceFieldMapper
extends|extends
name|AbstractFieldMapper
argument_list|<
name|byte
index|[]
argument_list|>
implements|implements
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|SourceFieldMapper
block|{
DECL|field|CONTENT_TYPE
specifier|public
specifier|static
specifier|final
name|String
name|CONTENT_TYPE
init|=
literal|"_source"
decl_stmt|;
DECL|class|Defaults
specifier|public
specifier|static
class|class
name|Defaults
extends|extends
name|AbstractFieldMapper
operator|.
name|Defaults
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|SourceFieldMapper
operator|.
name|NAME
decl_stmt|;
DECL|field|ENABLED
specifier|public
specifier|static
specifier|final
name|boolean
name|ENABLED
init|=
literal|true
decl_stmt|;
DECL|field|COMPRESS_THRESHOLD
specifier|public
specifier|static
specifier|final
name|long
name|COMPRESS_THRESHOLD
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|INDEX
specifier|public
specifier|static
specifier|final
name|Field
operator|.
name|Index
name|INDEX
init|=
name|Field
operator|.
name|Index
operator|.
name|NO
decl_stmt|;
DECL|field|STORE
specifier|public
specifier|static
specifier|final
name|Field
operator|.
name|Store
name|STORE
init|=
name|Field
operator|.
name|Store
operator|.
name|YES
decl_stmt|;
DECL|field|OMIT_NORMS
specifier|public
specifier|static
specifier|final
name|boolean
name|OMIT_NORMS
init|=
literal|true
decl_stmt|;
DECL|field|OMIT_TERM_FREQ_AND_POSITIONS
specifier|public
specifier|static
specifier|final
name|boolean
name|OMIT_TERM_FREQ_AND_POSITIONS
init|=
literal|true
decl_stmt|;
block|}
DECL|class|Builder
specifier|public
specifier|static
class|class
name|Builder
extends|extends
name|XContentMapper
operator|.
name|Builder
argument_list|<
name|Builder
argument_list|,
name|SourceFieldMapper
argument_list|>
block|{
DECL|field|enabled
specifier|private
name|boolean
name|enabled
init|=
name|Defaults
operator|.
name|ENABLED
decl_stmt|;
DECL|field|compressThreshold
specifier|private
name|long
name|compressThreshold
init|=
name|Defaults
operator|.
name|COMPRESS_THRESHOLD
decl_stmt|;
DECL|field|compress
specifier|private
name|Boolean
name|compress
init|=
literal|null
decl_stmt|;
DECL|method|Builder
specifier|public
name|Builder
parameter_list|()
block|{
name|super
argument_list|(
name|Defaults
operator|.
name|NAME
argument_list|)
expr_stmt|;
block|}
DECL|method|enabled
specifier|public
name|Builder
name|enabled
parameter_list|(
name|boolean
name|enabled
parameter_list|)
block|{
name|this
operator|.
name|enabled
operator|=
name|enabled
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|compress
specifier|public
name|Builder
name|compress
parameter_list|(
name|boolean
name|compress
parameter_list|)
block|{
name|this
operator|.
name|compress
operator|=
name|compress
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|compressThreshold
specifier|public
name|Builder
name|compressThreshold
parameter_list|(
name|long
name|compressThreshold
parameter_list|)
block|{
name|this
operator|.
name|compressThreshold
operator|=
name|compressThreshold
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|build
annotation|@
name|Override
specifier|public
name|SourceFieldMapper
name|build
parameter_list|(
name|BuilderContext
name|context
parameter_list|)
block|{
return|return
operator|new
name|SourceFieldMapper
argument_list|(
name|name
argument_list|,
name|enabled
argument_list|,
name|compress
argument_list|,
name|compressThreshold
argument_list|)
return|;
block|}
block|}
DECL|field|enabled
specifier|private
specifier|final
name|boolean
name|enabled
decl_stmt|;
DECL|field|compress
specifier|private
name|Boolean
name|compress
decl_stmt|;
DECL|field|compressThreshold
specifier|private
name|long
name|compressThreshold
decl_stmt|;
DECL|field|fieldSelector
specifier|private
specifier|final
name|SourceFieldSelector
name|fieldSelector
decl_stmt|;
DECL|method|SourceFieldMapper
specifier|protected
name|SourceFieldMapper
parameter_list|()
block|{
name|this
argument_list|(
name|Defaults
operator|.
name|NAME
argument_list|,
name|Defaults
operator|.
name|ENABLED
argument_list|,
literal|null
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
DECL|method|SourceFieldMapper
specifier|protected
name|SourceFieldMapper
parameter_list|(
name|String
name|name
parameter_list|,
name|boolean
name|enabled
parameter_list|,
name|Boolean
name|compress
parameter_list|,
name|long
name|compressThreshold
parameter_list|)
block|{
name|super
argument_list|(
operator|new
name|Names
argument_list|(
name|name
argument_list|,
name|name
argument_list|,
name|name
argument_list|,
name|name
argument_list|)
argument_list|,
name|Defaults
operator|.
name|INDEX
argument_list|,
name|Defaults
operator|.
name|STORE
argument_list|,
name|Defaults
operator|.
name|TERM_VECTOR
argument_list|,
name|Defaults
operator|.
name|BOOST
argument_list|,
name|Defaults
operator|.
name|OMIT_NORMS
argument_list|,
name|Defaults
operator|.
name|OMIT_TERM_FREQ_AND_POSITIONS
argument_list|,
name|Lucene
operator|.
name|KEYWORD_ANALYZER
argument_list|,
name|Lucene
operator|.
name|KEYWORD_ANALYZER
argument_list|)
expr_stmt|;
name|this
operator|.
name|enabled
operator|=
name|enabled
expr_stmt|;
name|this
operator|.
name|compress
operator|=
name|compress
expr_stmt|;
name|this
operator|.
name|compressThreshold
operator|=
name|compressThreshold
expr_stmt|;
name|this
operator|.
name|fieldSelector
operator|=
operator|new
name|SourceFieldSelector
argument_list|(
name|names
operator|.
name|indexName
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|enabled
specifier|public
name|boolean
name|enabled
parameter_list|()
block|{
return|return
name|this
operator|.
name|enabled
return|;
block|}
DECL|method|fieldSelector
specifier|public
name|FieldSelector
name|fieldSelector
parameter_list|()
block|{
return|return
name|this
operator|.
name|fieldSelector
return|;
block|}
DECL|method|parseCreateField
annotation|@
name|Override
specifier|protected
name|Field
name|parseCreateField
parameter_list|(
name|ParseContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|enabled
condition|)
block|{
return|return
literal|null
return|;
block|}
name|byte
index|[]
name|data
init|=
name|context
operator|.
name|source
argument_list|()
decl_stmt|;
if|if
condition|(
name|compress
operator|!=
literal|null
operator|&&
name|compress
operator|&&
operator|!
name|LZFDecoder
operator|.
name|isCompressed
argument_list|(
name|data
argument_list|)
condition|)
block|{
if|if
condition|(
name|compressThreshold
operator|==
operator|-
literal|1
operator|||
name|data
operator|.
name|length
operator|>
name|compressThreshold
condition|)
block|{
name|data
operator|=
name|LZFEncoder
operator|.
name|encodeWithCache
argument_list|(
name|data
argument_list|,
name|data
operator|.
name|length
argument_list|)
expr_stmt|;
name|context
operator|.
name|source
argument_list|(
name|data
argument_list|)
expr_stmt|;
block|}
block|}
return|return
operator|new
name|Field
argument_list|(
name|names
operator|.
name|indexName
argument_list|()
argument_list|,
name|data
argument_list|,
name|store
argument_list|)
return|;
block|}
DECL|method|value
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|value
parameter_list|(
name|Document
name|document
parameter_list|)
block|{
name|Fieldable
name|field
init|=
name|document
operator|.
name|getFieldable
argument_list|(
name|names
operator|.
name|indexName
argument_list|()
argument_list|)
decl_stmt|;
return|return
name|field
operator|==
literal|null
condition|?
literal|null
else|:
name|value
argument_list|(
name|field
argument_list|)
return|;
block|}
DECL|method|nativeValue
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|nativeValue
parameter_list|(
name|Fieldable
name|field
parameter_list|)
block|{
return|return
name|field
operator|.
name|getBinaryValue
argument_list|()
return|;
block|}
DECL|method|value
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|value
parameter_list|(
name|Fieldable
name|field
parameter_list|)
block|{
name|byte
index|[]
name|value
init|=
name|field
operator|.
name|getBinaryValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
return|return
name|value
return|;
block|}
if|if
condition|(
name|LZFDecoder
operator|.
name|isCompressed
argument_list|(
name|value
argument_list|)
condition|)
block|{
try|try
block|{
return|return
name|LZFDecoder
operator|.
name|decode
argument_list|(
name|value
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
name|ElasticSearchParseException
argument_list|(
literal|"failed to decompress source"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
return|return
name|value
return|;
block|}
DECL|method|valueFromString
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|valueFromString
parameter_list|(
name|String
name|value
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
DECL|method|valueAsString
annotation|@
name|Override
specifier|public
name|String
name|valueAsString
parameter_list|(
name|Fieldable
name|field
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
DECL|method|indexedValue
annotation|@
name|Override
specifier|public
name|String
name|indexedValue
parameter_list|(
name|String
name|value
parameter_list|)
block|{
return|return
name|value
return|;
block|}
DECL|class|SourceFieldSelector
specifier|private
specifier|static
class|class
name|SourceFieldSelector
implements|implements
name|FieldSelector
block|{
DECL|field|name
specifier|private
specifier|final
name|String
name|name
decl_stmt|;
DECL|method|SourceFieldSelector
specifier|private
name|SourceFieldSelector
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
block|}
DECL|method|accept
annotation|@
name|Override
specifier|public
name|FieldSelectorResult
name|accept
parameter_list|(
name|String
name|fieldName
parameter_list|)
block|{
if|if
condition|(
name|fieldName
operator|.
name|equals
argument_list|(
name|name
argument_list|)
condition|)
block|{
return|return
name|FieldSelectorResult
operator|.
name|LOAD_AND_BREAK
return|;
block|}
return|return
name|FieldSelectorResult
operator|.
name|NO_LOAD
return|;
block|}
block|}
DECL|method|contentType
annotation|@
name|Override
specifier|protected
name|String
name|contentType
parameter_list|()
block|{
return|return
name|CONTENT_TYPE
return|;
block|}
DECL|method|toXContent
annotation|@
name|Override
specifier|public
name|void
name|toXContent
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
comment|// all are defaults, no need to write it at all
if|if
condition|(
name|enabled
operator|==
name|Defaults
operator|.
name|ENABLED
operator|&&
name|compress
operator|==
literal|null
operator|&&
name|compressThreshold
operator|==
operator|-
literal|1
condition|)
block|{
return|return;
block|}
name|builder
operator|.
name|startObject
argument_list|(
name|contentType
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|enabled
operator|!=
name|Defaults
operator|.
name|ENABLED
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"enabled"
argument_list|,
name|enabled
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|compress
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"compress"
argument_list|,
name|compress
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|compressThreshold
operator|!=
operator|-
literal|1
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"compress_threshold"
argument_list|,
operator|new
name|ByteSizeValue
argument_list|(
name|compressThreshold
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
DECL|method|merge
annotation|@
name|Override
specifier|public
name|void
name|merge
parameter_list|(
name|XContentMapper
name|mergeWith
parameter_list|,
name|MergeContext
name|mergeContext
parameter_list|)
throws|throws
name|MergeMappingException
block|{
name|SourceFieldMapper
name|sourceMergeWith
init|=
operator|(
name|SourceFieldMapper
operator|)
name|mergeWith
decl_stmt|;
if|if
condition|(
operator|!
name|mergeContext
operator|.
name|mergeFlags
argument_list|()
operator|.
name|simulate
argument_list|()
condition|)
block|{
if|if
condition|(
name|sourceMergeWith
operator|.
name|compress
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|compress
operator|=
name|sourceMergeWith
operator|.
name|compress
expr_stmt|;
block|}
if|if
condition|(
name|sourceMergeWith
operator|.
name|compressThreshold
operator|!=
operator|-
literal|1
condition|)
block|{
name|this
operator|.
name|compressThreshold
operator|=
name|sourceMergeWith
operator|.
name|compressThreshold
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

