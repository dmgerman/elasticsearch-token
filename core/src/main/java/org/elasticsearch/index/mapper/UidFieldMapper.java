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
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|document
operator|.
name|BinaryDocValuesField
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
name|document
operator|.
name|Field
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
name|IndexOptions
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
name|IndexableField
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
name|settings
operator|.
name|Settings
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
name|fielddata
operator|.
name|IndexFieldData
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
name|plain
operator|.
name|PagedBytesIndexFieldData
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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

begin_class
DECL|class|UidFieldMapper
specifier|public
class|class
name|UidFieldMapper
extends|extends
name|MetadataFieldMapper
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"_uid"
decl_stmt|;
DECL|field|CONTENT_TYPE
specifier|public
specifier|static
specifier|final
name|String
name|CONTENT_TYPE
init|=
literal|"_uid"
decl_stmt|;
DECL|class|Defaults
specifier|public
specifier|static
class|class
name|Defaults
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
name|UidFieldMapper
operator|.
name|NAME
decl_stmt|;
DECL|field|FIELD_TYPE
specifier|public
specifier|static
specifier|final
name|MappedFieldType
name|FIELD_TYPE
init|=
operator|new
name|UidFieldType
argument_list|()
decl_stmt|;
DECL|field|NESTED_FIELD_TYPE
specifier|public
specifier|static
specifier|final
name|MappedFieldType
name|NESTED_FIELD_TYPE
decl_stmt|;
static|static
block|{
name|FIELD_TYPE
operator|.
name|setIndexOptions
argument_list|(
name|IndexOptions
operator|.
name|DOCS
argument_list|)
expr_stmt|;
name|FIELD_TYPE
operator|.
name|setTokenized
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|FIELD_TYPE
operator|.
name|setStored
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|FIELD_TYPE
operator|.
name|setOmitNorms
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|FIELD_TYPE
operator|.
name|setIndexAnalyzer
argument_list|(
name|Lucene
operator|.
name|KEYWORD_ANALYZER
argument_list|)
expr_stmt|;
name|FIELD_TYPE
operator|.
name|setSearchAnalyzer
argument_list|(
name|Lucene
operator|.
name|KEYWORD_ANALYZER
argument_list|)
expr_stmt|;
name|FIELD_TYPE
operator|.
name|setName
argument_list|(
name|NAME
argument_list|)
expr_stmt|;
name|FIELD_TYPE
operator|.
name|freeze
argument_list|()
expr_stmt|;
name|NESTED_FIELD_TYPE
operator|=
name|FIELD_TYPE
operator|.
name|clone
argument_list|()
expr_stmt|;
name|NESTED_FIELD_TYPE
operator|.
name|setStored
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|NESTED_FIELD_TYPE
operator|.
name|freeze
argument_list|()
expr_stmt|;
block|}
block|}
DECL|class|TypeParser
specifier|public
specifier|static
class|class
name|TypeParser
implements|implements
name|MetadataFieldMapper
operator|.
name|TypeParser
block|{
annotation|@
name|Override
DECL|method|parse
specifier|public
name|MetadataFieldMapper
operator|.
name|Builder
argument_list|<
name|?
argument_list|,
name|?
argument_list|>
name|parse
parameter_list|(
name|String
name|name
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|node
parameter_list|,
name|ParserContext
name|parserContext
parameter_list|)
throws|throws
name|MapperParsingException
block|{
throw|throw
operator|new
name|MapperParsingException
argument_list|(
name|NAME
operator|+
literal|" is not configurable"
argument_list|)
throw|;
block|}
annotation|@
name|Override
DECL|method|getDefault
specifier|public
name|MetadataFieldMapper
name|getDefault
parameter_list|(
name|MappedFieldType
name|fieldType
parameter_list|,
name|ParserContext
name|context
parameter_list|)
block|{
specifier|final
name|Settings
name|indexSettings
init|=
name|context
operator|.
name|mapperService
argument_list|()
operator|.
name|getIndexSettings
argument_list|()
operator|.
name|getSettings
argument_list|()
decl_stmt|;
return|return
operator|new
name|UidFieldMapper
argument_list|(
name|indexSettings
argument_list|,
name|fieldType
argument_list|)
return|;
block|}
block|}
DECL|class|UidFieldType
specifier|static
specifier|final
class|class
name|UidFieldType
extends|extends
name|TermBasedFieldType
block|{
DECL|method|UidFieldType
name|UidFieldType
parameter_list|()
block|{         }
DECL|method|UidFieldType
specifier|protected
name|UidFieldType
parameter_list|(
name|UidFieldType
name|ref
parameter_list|)
block|{
name|super
argument_list|(
name|ref
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|clone
specifier|public
name|MappedFieldType
name|clone
parameter_list|()
block|{
return|return
operator|new
name|UidFieldType
argument_list|(
name|this
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|typeName
specifier|public
name|String
name|typeName
parameter_list|()
block|{
return|return
name|CONTENT_TYPE
return|;
block|}
annotation|@
name|Override
DECL|method|fielddataBuilder
specifier|public
name|IndexFieldData
operator|.
name|Builder
name|fielddataBuilder
parameter_list|()
block|{
comment|// TODO: add doc values support?
return|return
operator|new
name|PagedBytesIndexFieldData
operator|.
name|Builder
argument_list|(
name|TextFieldMapper
operator|.
name|Defaults
operator|.
name|FIELDDATA_MIN_FREQUENCY
argument_list|,
name|TextFieldMapper
operator|.
name|Defaults
operator|.
name|FIELDDATA_MAX_FREQUENCY
argument_list|,
name|TextFieldMapper
operator|.
name|Defaults
operator|.
name|FIELDDATA_MIN_SEGMENT_SIZE
argument_list|)
return|;
block|}
block|}
DECL|method|UidFieldMapper
specifier|private
name|UidFieldMapper
parameter_list|(
name|Settings
name|indexSettings
parameter_list|,
name|MappedFieldType
name|existing
parameter_list|)
block|{
name|this
argument_list|(
name|existing
operator|==
literal|null
condition|?
name|Defaults
operator|.
name|FIELD_TYPE
operator|.
name|clone
argument_list|()
else|:
name|existing
argument_list|,
name|Defaults
operator|.
name|FIELD_TYPE
argument_list|,
name|indexSettings
argument_list|)
expr_stmt|;
block|}
DECL|method|UidFieldMapper
specifier|private
name|UidFieldMapper
parameter_list|(
name|MappedFieldType
name|fieldType
parameter_list|,
name|MappedFieldType
name|defaultFieldType
parameter_list|,
name|Settings
name|indexSettings
parameter_list|)
block|{
name|super
argument_list|(
name|NAME
argument_list|,
name|fieldType
argument_list|,
name|defaultFieldType
argument_list|,
name|indexSettings
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|preParse
specifier|public
name|void
name|preParse
parameter_list|(
name|ParseContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|super
operator|.
name|parse
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|postParse
specifier|public
name|void
name|postParse
parameter_list|(
name|ParseContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{}
annotation|@
name|Override
DECL|method|parse
specifier|public
name|Mapper
name|parse
parameter_list|(
name|ParseContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
comment|// nothing to do here, we do everything in preParse
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|parseCreateField
specifier|protected
name|void
name|parseCreateField
parameter_list|(
name|ParseContext
name|context
parameter_list|,
name|List
argument_list|<
name|IndexableField
argument_list|>
name|fields
parameter_list|)
throws|throws
name|IOException
block|{
name|Field
name|uid
init|=
operator|new
name|Field
argument_list|(
name|NAME
argument_list|,
name|Uid
operator|.
name|createUid
argument_list|(
name|context
operator|.
name|sourceToParse
argument_list|()
operator|.
name|type
argument_list|()
argument_list|,
name|context
operator|.
name|sourceToParse
argument_list|()
operator|.
name|id
argument_list|()
argument_list|)
argument_list|,
name|Defaults
operator|.
name|FIELD_TYPE
argument_list|)
decl_stmt|;
name|fields
operator|.
name|add
argument_list|(
name|uid
argument_list|)
expr_stmt|;
if|if
condition|(
name|fieldType
argument_list|()
operator|.
name|hasDocValues
argument_list|()
condition|)
block|{
name|fields
operator|.
name|add
argument_list|(
operator|new
name|BinaryDocValuesField
argument_list|(
name|NAME
argument_list|,
operator|new
name|BytesRef
argument_list|(
name|uid
operator|.
name|stringValue
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|contentType
specifier|protected
name|String
name|contentType
parameter_list|()
block|{
return|return
name|CONTENT_TYPE
return|;
block|}
annotation|@
name|Override
DECL|method|toXContent
specifier|public
name|XContentBuilder
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
return|return
name|builder
return|;
block|}
annotation|@
name|Override
DECL|method|doMerge
specifier|protected
name|void
name|doMerge
parameter_list|(
name|Mapper
name|mergeWith
parameter_list|,
name|boolean
name|updateAllTypes
parameter_list|)
block|{
comment|// do nothing here, no merging, but also no exception
block|}
block|}
end_class

end_unit

