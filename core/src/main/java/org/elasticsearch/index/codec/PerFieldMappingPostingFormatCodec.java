begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.codec
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|codec
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
name|codecs
operator|.
name|Codec
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
name|codecs
operator|.
name|PostingsFormat
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
name|codecs
operator|.
name|lucene50
operator|.
name|Lucene50StoredFieldsFormat
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
name|codecs
operator|.
name|lucene54
operator|.
name|Lucene54Codec
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
name|logging
operator|.
name|ESLogger
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
name|index
operator|.
name|mapper
operator|.
name|MappedFieldType
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
name|MapperService
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
name|core
operator|.
name|CompletionFieldMapper
import|;
end_import

begin_comment
comment|/**  * {@link PerFieldMappingPostingFormatCodec This postings format} is the default  * {@link PostingsFormat} for Elasticsearch. It utilizes the  * {@link MapperService} to lookup a {@link PostingsFormat} per field. This  * allows users to change the low level postings format for individual fields  * per index in real time via the mapping API. If no specific postings format is  * configured for a specific field the default postings format is used.  */
end_comment

begin_comment
comment|// LUCENE UPGRADE: make sure to move to a new codec depending on the lucene version
end_comment

begin_class
DECL|class|PerFieldMappingPostingFormatCodec
specifier|public
class|class
name|PerFieldMappingPostingFormatCodec
extends|extends
name|Lucene54Codec
block|{
DECL|field|logger
specifier|private
specifier|final
name|ESLogger
name|logger
decl_stmt|;
DECL|field|mapperService
specifier|private
specifier|final
name|MapperService
name|mapperService
decl_stmt|;
static|static
block|{
assert|assert
name|Codec
operator|.
name|forName
argument_list|(
name|Lucene
operator|.
name|LATEST_CODEC
argument_list|)
operator|.
name|getClass
argument_list|()
operator|.
name|isAssignableFrom
argument_list|(
name|PerFieldMappingPostingFormatCodec
operator|.
name|class
argument_list|)
operator|:
literal|"PerFieldMappingPostingFormatCodec must subclass the latest lucene codec: "
operator|+
name|Lucene
operator|.
name|LATEST_CODEC
assert|;
block|}
DECL|method|PerFieldMappingPostingFormatCodec
specifier|public
name|PerFieldMappingPostingFormatCodec
parameter_list|(
name|Lucene50StoredFieldsFormat
operator|.
name|Mode
name|compressionMode
parameter_list|,
name|MapperService
name|mapperService
parameter_list|,
name|ESLogger
name|logger
parameter_list|)
block|{
name|super
argument_list|(
name|compressionMode
argument_list|)
expr_stmt|;
name|this
operator|.
name|mapperService
operator|=
name|mapperService
expr_stmt|;
name|this
operator|.
name|logger
operator|=
name|logger
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getPostingsFormatForField
specifier|public
name|PostingsFormat
name|getPostingsFormatForField
parameter_list|(
name|String
name|field
parameter_list|)
block|{
specifier|final
name|MappedFieldType
name|indexName
init|=
name|mapperService
operator|.
name|indexName
argument_list|(
name|field
argument_list|)
decl_stmt|;
if|if
condition|(
name|indexName
operator|==
literal|null
condition|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"no index mapper found for field: [{}] returning default postings format"
argument_list|,
name|field
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|indexName
operator|instanceof
name|CompletionFieldMapper
operator|.
name|CompletionFieldType
condition|)
block|{
return|return
name|CompletionFieldMapper
operator|.
name|CompletionFieldType
operator|.
name|postingsFormat
argument_list|()
return|;
block|}
return|return
name|super
operator|.
name|getPostingsFormatForField
argument_list|(
name|field
argument_list|)
return|;
block|}
block|}
end_class

end_unit

