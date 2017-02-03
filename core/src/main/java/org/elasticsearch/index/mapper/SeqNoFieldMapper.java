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
name|document
operator|.
name|LongPoint
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
name|NumericDocValuesField
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
name|SortedNumericDocValuesField
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
name|DocValuesType
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
name|IndexReader
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
name|NumericDocValues
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
name|PointValues
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
name|search
operator|.
name|BoostQuery
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
name|search
operator|.
name|MatchNoDocsQuery
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
name|search
operator|.
name|Query
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
name|Bits
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
name|action
operator|.
name|fieldstats
operator|.
name|FieldStats
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
name|Nullable
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
name|common
operator|.
name|xcontent
operator|.
name|XContentParser
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
name|IndexNumericFieldData
operator|.
name|NumericType
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
name|DocValuesIndexFieldData
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
name|Mapper
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
name|MapperParsingException
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
name|MetadataFieldMapper
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
name|ParseContext
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
name|Mapper
operator|.
name|TypeParser
operator|.
name|ParserContext
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
name|ParseContext
operator|.
name|Document
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
name|query
operator|.
name|QueryShardContext
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
name|query
operator|.
name|QueryShardException
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
name|seqno
operator|.
name|SequenceNumbersService
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Objects
import|;
end_import

begin_comment
comment|/**  * Mapper for the {@code _seq_no} field.  *  * We expect to use the seq# for sorting, during collision checking and for  * doing range searches. Therefore the {@code _seq_no} field is stored both  * as a numeric doc value and as numeric indexed field.  *  * This mapper also manages the primary term field, which has no ES named  * equivalent. The primary term is only used during collision after receiving  * identical seq# values for two document copies. The primary term is stored as  * a doc value field without being indexed, since it is only intended for use  * as a key-value lookup.   */
end_comment

begin_class
DECL|class|SeqNoFieldMapper
specifier|public
class|class
name|SeqNoFieldMapper
extends|extends
name|MetadataFieldMapper
block|{
comment|/**      * A sequence ID, which is made up of a sequence number (both the searchable      * and doc_value version of the field) and the primary term.      */
DECL|class|SequenceID
specifier|public
specifier|static
class|class
name|SequenceID
block|{
DECL|field|seqNo
specifier|public
specifier|final
name|Field
name|seqNo
decl_stmt|;
DECL|field|seqNoDocValue
specifier|public
specifier|final
name|Field
name|seqNoDocValue
decl_stmt|;
DECL|field|primaryTerm
specifier|public
specifier|final
name|Field
name|primaryTerm
decl_stmt|;
DECL|method|SequenceID
specifier|public
name|SequenceID
parameter_list|(
name|Field
name|seqNo
parameter_list|,
name|Field
name|seqNoDocValue
parameter_list|,
name|Field
name|primaryTerm
parameter_list|)
block|{
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|seqNo
argument_list|,
literal|"sequence number field cannot be null"
argument_list|)
expr_stmt|;
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|seqNoDocValue
argument_list|,
literal|"sequence number dv field cannot be null"
argument_list|)
expr_stmt|;
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|primaryTerm
argument_list|,
literal|"primary term field cannot be null"
argument_list|)
expr_stmt|;
name|this
operator|.
name|seqNo
operator|=
name|seqNo
expr_stmt|;
name|this
operator|.
name|seqNoDocValue
operator|=
name|seqNoDocValue
expr_stmt|;
name|this
operator|.
name|primaryTerm
operator|=
name|primaryTerm
expr_stmt|;
block|}
DECL|method|emptySeqID
specifier|public
specifier|static
name|SequenceID
name|emptySeqID
parameter_list|()
block|{
return|return
operator|new
name|SequenceID
argument_list|(
operator|new
name|LongPoint
argument_list|(
name|NAME
argument_list|,
name|SequenceNumbersService
operator|.
name|UNASSIGNED_SEQ_NO
argument_list|)
argument_list|,
operator|new
name|SortedNumericDocValuesField
argument_list|(
name|NAME
argument_list|,
name|SequenceNumbersService
operator|.
name|UNASSIGNED_SEQ_NO
argument_list|)
argument_list|,
operator|new
name|NumericDocValuesField
argument_list|(
name|PRIMARY_TERM_NAME
argument_list|,
literal|0
argument_list|)
argument_list|)
return|;
block|}
block|}
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"_seq_no"
decl_stmt|;
DECL|field|CONTENT_TYPE
specifier|public
specifier|static
specifier|final
name|String
name|CONTENT_TYPE
init|=
literal|"_seq_no"
decl_stmt|;
DECL|field|PRIMARY_TERM_NAME
specifier|public
specifier|static
specifier|final
name|String
name|PRIMARY_TERM_NAME
init|=
literal|"_primary_term"
decl_stmt|;
DECL|class|SeqNoDefaults
specifier|public
specifier|static
class|class
name|SeqNoDefaults
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
name|SeqNoFieldMapper
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
name|SeqNoFieldType
argument_list|()
decl_stmt|;
static|static
block|{
name|FIELD_TYPE
operator|.
name|setName
argument_list|(
name|NAME
argument_list|)
expr_stmt|;
name|FIELD_TYPE
operator|.
name|setDocValuesType
argument_list|(
name|DocValuesType
operator|.
name|SORTED
argument_list|)
expr_stmt|;
name|FIELD_TYPE
operator|.
name|setHasDocValues
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|FIELD_TYPE
operator|.
name|freeze
argument_list|()
expr_stmt|;
block|}
block|}
DECL|class|Builder
specifier|public
specifier|static
class|class
name|Builder
extends|extends
name|MetadataFieldMapper
operator|.
name|Builder
argument_list|<
name|Builder
argument_list|,
name|SeqNoFieldMapper
argument_list|>
block|{
DECL|method|Builder
specifier|public
name|Builder
parameter_list|()
block|{
name|super
argument_list|(
name|SeqNoDefaults
operator|.
name|NAME
argument_list|,
name|SeqNoDefaults
operator|.
name|FIELD_TYPE
argument_list|,
name|SeqNoDefaults
operator|.
name|FIELD_TYPE
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|build
specifier|public
name|SeqNoFieldMapper
name|build
parameter_list|(
name|BuilderContext
name|context
parameter_list|)
block|{
return|return
operator|new
name|SeqNoFieldMapper
argument_list|(
name|context
operator|.
name|indexSettings
argument_list|()
argument_list|)
return|;
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
name|SeqNoFieldMapper
argument_list|(
name|indexSettings
argument_list|)
return|;
block|}
block|}
DECL|class|SeqNoFieldType
specifier|static
specifier|final
class|class
name|SeqNoFieldType
extends|extends
name|MappedFieldType
block|{
DECL|method|SeqNoFieldType
name|SeqNoFieldType
parameter_list|()
block|{         }
DECL|method|SeqNoFieldType
specifier|protected
name|SeqNoFieldType
parameter_list|(
name|SeqNoFieldType
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
name|SeqNoFieldType
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
DECL|method|parse
specifier|private
name|long
name|parse
parameter_list|(
name|Object
name|value
parameter_list|)
block|{
if|if
condition|(
name|value
operator|instanceof
name|Number
condition|)
block|{
name|double
name|doubleValue
init|=
operator|(
operator|(
name|Number
operator|)
name|value
operator|)
operator|.
name|doubleValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|doubleValue
argument_list|<
name|Long
operator|.
name|MIN_VALUE
operator|||
name|doubleValue
argument_list|>
name|Long
operator|.
name|MAX_VALUE
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Value ["
operator|+
name|value
operator|+
literal|"] is out of range for a long"
argument_list|)
throw|;
block|}
if|if
condition|(
name|doubleValue
operator|%
literal|1
operator|!=
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Value ["
operator|+
name|value
operator|+
literal|"] has a decimal part"
argument_list|)
throw|;
block|}
return|return
operator|(
operator|(
name|Number
operator|)
name|value
operator|)
operator|.
name|longValue
argument_list|()
return|;
block|}
if|if
condition|(
name|value
operator|instanceof
name|BytesRef
condition|)
block|{
name|value
operator|=
operator|(
operator|(
name|BytesRef
operator|)
name|value
operator|)
operator|.
name|utf8ToString
argument_list|()
expr_stmt|;
block|}
return|return
name|Long
operator|.
name|parseLong
argument_list|(
name|value
operator|.
name|toString
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|termQuery
specifier|public
name|Query
name|termQuery
parameter_list|(
name|Object
name|value
parameter_list|,
annotation|@
name|Nullable
name|QueryShardContext
name|context
parameter_list|)
block|{
name|long
name|v
init|=
name|parse
argument_list|(
name|value
argument_list|)
decl_stmt|;
return|return
name|LongPoint
operator|.
name|newExactQuery
argument_list|(
name|name
argument_list|()
argument_list|,
name|v
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|termsQuery
specifier|public
name|Query
name|termsQuery
parameter_list|(
name|List
argument_list|<
name|?
argument_list|>
name|values
parameter_list|,
annotation|@
name|Nullable
name|QueryShardContext
name|context
parameter_list|)
block|{
name|long
index|[]
name|v
init|=
operator|new
name|long
index|[
name|values
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|values
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|v
index|[
name|i
index|]
operator|=
name|parse
argument_list|(
name|values
operator|.
name|get
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|LongPoint
operator|.
name|newSetQuery
argument_list|(
name|name
argument_list|()
argument_list|,
name|v
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|rangeQuery
specifier|public
name|Query
name|rangeQuery
parameter_list|(
name|Object
name|lowerTerm
parameter_list|,
name|Object
name|upperTerm
parameter_list|,
name|boolean
name|includeLower
parameter_list|,
name|boolean
name|includeUpper
parameter_list|,
name|QueryShardContext
name|context
parameter_list|)
block|{
name|long
name|l
init|=
name|Long
operator|.
name|MIN_VALUE
decl_stmt|;
name|long
name|u
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
if|if
condition|(
name|lowerTerm
operator|!=
literal|null
condition|)
block|{
name|l
operator|=
name|parse
argument_list|(
name|lowerTerm
argument_list|)
expr_stmt|;
if|if
condition|(
name|includeLower
operator|==
literal|false
condition|)
block|{
if|if
condition|(
name|l
operator|==
name|Long
operator|.
name|MAX_VALUE
condition|)
block|{
return|return
operator|new
name|MatchNoDocsQuery
argument_list|()
return|;
block|}
operator|++
name|l
expr_stmt|;
block|}
block|}
if|if
condition|(
name|upperTerm
operator|!=
literal|null
condition|)
block|{
name|u
operator|=
name|parse
argument_list|(
name|upperTerm
argument_list|)
expr_stmt|;
if|if
condition|(
name|includeUpper
operator|==
literal|false
condition|)
block|{
if|if
condition|(
name|u
operator|==
name|Long
operator|.
name|MIN_VALUE
condition|)
block|{
return|return
operator|new
name|MatchNoDocsQuery
argument_list|()
return|;
block|}
operator|--
name|u
expr_stmt|;
block|}
block|}
return|return
name|LongPoint
operator|.
name|newRangeQuery
argument_list|(
name|name
argument_list|()
argument_list|,
name|l
argument_list|,
name|u
argument_list|)
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
name|failIfNoDocValues
argument_list|()
expr_stmt|;
return|return
operator|new
name|DocValuesIndexFieldData
operator|.
name|Builder
argument_list|()
operator|.
name|numericType
argument_list|(
name|NumericType
operator|.
name|LONG
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|stats
specifier|public
name|FieldStats
name|stats
parameter_list|(
name|IndexReader
name|reader
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|fieldName
init|=
name|name
argument_list|()
decl_stmt|;
name|long
name|size
init|=
name|PointValues
operator|.
name|size
argument_list|(
name|reader
argument_list|,
name|fieldName
argument_list|)
decl_stmt|;
if|if
condition|(
name|size
operator|==
literal|0
condition|)
block|{
return|return
literal|null
return|;
block|}
name|int
name|docCount
init|=
name|PointValues
operator|.
name|getDocCount
argument_list|(
name|reader
argument_list|,
name|fieldName
argument_list|)
decl_stmt|;
name|byte
index|[]
name|min
init|=
name|PointValues
operator|.
name|getMinPackedValue
argument_list|(
name|reader
argument_list|,
name|fieldName
argument_list|)
decl_stmt|;
name|byte
index|[]
name|max
init|=
name|PointValues
operator|.
name|getMaxPackedValue
argument_list|(
name|reader
argument_list|,
name|fieldName
argument_list|)
decl_stmt|;
return|return
operator|new
name|FieldStats
operator|.
name|Long
argument_list|(
name|reader
operator|.
name|maxDoc
argument_list|()
argument_list|,
name|docCount
argument_list|,
operator|-
literal|1L
argument_list|,
name|size
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|,
name|LongPoint
operator|.
name|decodeDimension
argument_list|(
name|min
argument_list|,
literal|0
argument_list|)
argument_list|,
name|LongPoint
operator|.
name|decodeDimension
argument_list|(
name|max
argument_list|,
literal|0
argument_list|)
argument_list|)
return|;
block|}
block|}
DECL|method|SeqNoFieldMapper
specifier|public
name|SeqNoFieldMapper
parameter_list|(
name|Settings
name|indexSettings
parameter_list|)
block|{
name|super
argument_list|(
name|NAME
argument_list|,
name|SeqNoDefaults
operator|.
name|FIELD_TYPE
argument_list|,
name|SeqNoDefaults
operator|.
name|FIELD_TYPE
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
comment|// see InternalEngine.innerIndex to see where the real version value is set
comment|// also see ParsedDocument.updateSeqID (called by innerIndex)
name|SequenceID
name|seqID
init|=
name|SequenceID
operator|.
name|emptySeqID
argument_list|()
decl_stmt|;
name|context
operator|.
name|seqID
argument_list|(
name|seqID
argument_list|)
expr_stmt|;
name|fields
operator|.
name|add
argument_list|(
name|seqID
operator|.
name|seqNo
argument_list|)
expr_stmt|;
name|fields
operator|.
name|add
argument_list|(
name|seqID
operator|.
name|seqNoDocValue
argument_list|)
expr_stmt|;
name|fields
operator|.
name|add
argument_list|(
name|seqID
operator|.
name|primaryTerm
argument_list|)
expr_stmt|;
block|}
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
comment|// fields are added in parseCreateField
return|return
literal|null
return|;
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
block|{
comment|// In the case of nested docs, let's fill nested docs with seqNo=1 and
comment|// primaryTerm=0 so that Lucene doesn't write a Bitset for documents
comment|// that don't have the field. This is consistent with the default value
comment|// for efficiency.
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<
name|context
operator|.
name|docs
argument_list|()
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
specifier|final
name|Document
name|doc
init|=
name|context
operator|.
name|docs
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|doc
operator|.
name|add
argument_list|(
operator|new
name|LongPoint
argument_list|(
name|NAME
argument_list|,
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|doc
operator|.
name|add
argument_list|(
operator|new
name|SortedNumericDocValuesField
argument_list|(
name|NAME
argument_list|,
literal|1L
argument_list|)
argument_list|)
expr_stmt|;
name|doc
operator|.
name|add
argument_list|(
operator|new
name|NumericDocValuesField
argument_list|(
name|PRIMARY_TERM_NAME
argument_list|,
literal|0L
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
comment|// nothing to do
block|}
block|}
end_class

end_unit

