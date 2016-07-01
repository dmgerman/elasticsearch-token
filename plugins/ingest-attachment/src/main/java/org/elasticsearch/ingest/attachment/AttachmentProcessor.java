begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.ingest.attachment
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|attachment
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tika
operator|.
name|language
operator|.
name|LanguageIdentifier
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tika
operator|.
name|metadata
operator|.
name|Metadata
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|tika
operator|.
name|metadata
operator|.
name|TikaCoreProperties
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchParseException
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
name|Strings
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|AbstractProcessor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|IngestDocument
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|Processor
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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|EnumSet
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
name|Locale
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
name|Set
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|ConfigurationUtils
operator|.
name|newConfigurationException
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|ConfigurationUtils
operator|.
name|readIntProperty
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|ConfigurationUtils
operator|.
name|readOptionalList
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|ConfigurationUtils
operator|.
name|readStringProperty
import|;
end_import

begin_class
DECL|class|AttachmentProcessor
specifier|public
specifier|final
class|class
name|AttachmentProcessor
extends|extends
name|AbstractProcessor
block|{
DECL|field|TYPE
specifier|public
specifier|static
specifier|final
name|String
name|TYPE
init|=
literal|"attachment"
decl_stmt|;
DECL|field|NUMBER_OF_CHARS_INDEXED
specifier|private
specifier|static
specifier|final
name|int
name|NUMBER_OF_CHARS_INDEXED
init|=
literal|100000
decl_stmt|;
DECL|field|field
specifier|private
specifier|final
name|String
name|field
decl_stmt|;
DECL|field|targetField
specifier|private
specifier|final
name|String
name|targetField
decl_stmt|;
DECL|field|properties
specifier|private
specifier|final
name|Set
argument_list|<
name|Property
argument_list|>
name|properties
decl_stmt|;
DECL|field|indexedChars
specifier|private
specifier|final
name|int
name|indexedChars
decl_stmt|;
DECL|method|AttachmentProcessor
name|AttachmentProcessor
parameter_list|(
name|String
name|tag
parameter_list|,
name|String
name|field
parameter_list|,
name|String
name|targetField
parameter_list|,
name|Set
argument_list|<
name|Property
argument_list|>
name|properties
parameter_list|,
name|int
name|indexedChars
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|tag
argument_list|)
expr_stmt|;
name|this
operator|.
name|field
operator|=
name|field
expr_stmt|;
name|this
operator|.
name|targetField
operator|=
name|targetField
expr_stmt|;
name|this
operator|.
name|properties
operator|=
name|properties
expr_stmt|;
name|this
operator|.
name|indexedChars
operator|=
name|indexedChars
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|execute
specifier|public
name|void
name|execute
parameter_list|(
name|IngestDocument
name|ingestDocument
parameter_list|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|additionalFields
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
try|try
block|{
name|Metadata
name|metadata
init|=
operator|new
name|Metadata
argument_list|()
decl_stmt|;
name|byte
index|[]
name|input
init|=
name|ingestDocument
operator|.
name|getFieldValueAsBytes
argument_list|(
name|field
argument_list|)
decl_stmt|;
name|String
name|parsedContent
init|=
name|TikaImpl
operator|.
name|parse
argument_list|(
name|input
argument_list|,
name|metadata
argument_list|,
name|indexedChars
argument_list|)
decl_stmt|;
if|if
condition|(
name|properties
operator|.
name|contains
argument_list|(
name|Property
operator|.
name|CONTENT
argument_list|)
operator|&&
name|Strings
operator|.
name|hasLength
argument_list|(
name|parsedContent
argument_list|)
condition|)
block|{
comment|// somehow tika seems to append a newline at the end automatically, lets remove that again
name|additionalFields
operator|.
name|put
argument_list|(
name|Property
operator|.
name|CONTENT
operator|.
name|toLowerCase
argument_list|()
argument_list|,
name|parsedContent
operator|.
name|trim
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|properties
operator|.
name|contains
argument_list|(
name|Property
operator|.
name|LANGUAGE
argument_list|)
operator|&&
name|Strings
operator|.
name|hasLength
argument_list|(
name|parsedContent
argument_list|)
condition|)
block|{
name|LanguageIdentifier
name|identifier
init|=
operator|new
name|LanguageIdentifier
argument_list|(
name|parsedContent
argument_list|)
decl_stmt|;
name|String
name|language
init|=
name|identifier
operator|.
name|getLanguage
argument_list|()
decl_stmt|;
name|additionalFields
operator|.
name|put
argument_list|(
name|Property
operator|.
name|LANGUAGE
operator|.
name|toLowerCase
argument_list|()
argument_list|,
name|language
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|properties
operator|.
name|contains
argument_list|(
name|Property
operator|.
name|DATE
argument_list|)
condition|)
block|{
name|String
name|createdDate
init|=
name|metadata
operator|.
name|get
argument_list|(
name|TikaCoreProperties
operator|.
name|CREATED
argument_list|)
decl_stmt|;
if|if
condition|(
name|createdDate
operator|!=
literal|null
condition|)
block|{
name|additionalFields
operator|.
name|put
argument_list|(
name|Property
operator|.
name|DATE
operator|.
name|toLowerCase
argument_list|()
argument_list|,
name|createdDate
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|properties
operator|.
name|contains
argument_list|(
name|Property
operator|.
name|TITLE
argument_list|)
condition|)
block|{
name|String
name|title
init|=
name|metadata
operator|.
name|get
argument_list|(
name|TikaCoreProperties
operator|.
name|TITLE
argument_list|)
decl_stmt|;
if|if
condition|(
name|Strings
operator|.
name|hasLength
argument_list|(
name|title
argument_list|)
condition|)
block|{
name|additionalFields
operator|.
name|put
argument_list|(
name|Property
operator|.
name|TITLE
operator|.
name|toLowerCase
argument_list|()
argument_list|,
name|title
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|properties
operator|.
name|contains
argument_list|(
name|Property
operator|.
name|AUTHOR
argument_list|)
condition|)
block|{
name|String
name|author
init|=
name|metadata
operator|.
name|get
argument_list|(
literal|"Author"
argument_list|)
decl_stmt|;
if|if
condition|(
name|Strings
operator|.
name|hasLength
argument_list|(
name|author
argument_list|)
condition|)
block|{
name|additionalFields
operator|.
name|put
argument_list|(
name|Property
operator|.
name|AUTHOR
operator|.
name|toLowerCase
argument_list|()
argument_list|,
name|author
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|properties
operator|.
name|contains
argument_list|(
name|Property
operator|.
name|KEYWORDS
argument_list|)
condition|)
block|{
name|String
name|keywords
init|=
name|metadata
operator|.
name|get
argument_list|(
literal|"Keywords"
argument_list|)
decl_stmt|;
if|if
condition|(
name|Strings
operator|.
name|hasLength
argument_list|(
name|keywords
argument_list|)
condition|)
block|{
name|additionalFields
operator|.
name|put
argument_list|(
name|Property
operator|.
name|KEYWORDS
operator|.
name|toLowerCase
argument_list|()
argument_list|,
name|keywords
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|properties
operator|.
name|contains
argument_list|(
name|Property
operator|.
name|CONTENT_TYPE
argument_list|)
condition|)
block|{
name|String
name|contentType
init|=
name|metadata
operator|.
name|get
argument_list|(
name|Metadata
operator|.
name|CONTENT_TYPE
argument_list|)
decl_stmt|;
if|if
condition|(
name|Strings
operator|.
name|hasLength
argument_list|(
name|contentType
argument_list|)
condition|)
block|{
name|additionalFields
operator|.
name|put
argument_list|(
name|Property
operator|.
name|CONTENT_TYPE
operator|.
name|toLowerCase
argument_list|()
argument_list|,
name|contentType
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|properties
operator|.
name|contains
argument_list|(
name|Property
operator|.
name|CONTENT_LENGTH
argument_list|)
condition|)
block|{
name|String
name|contentLength
init|=
name|metadata
operator|.
name|get
argument_list|(
name|Metadata
operator|.
name|CONTENT_LENGTH
argument_list|)
decl_stmt|;
name|String
name|length
init|=
name|Strings
operator|.
name|hasLength
argument_list|(
name|contentLength
argument_list|)
condition|?
name|contentLength
else|:
name|String
operator|.
name|valueOf
argument_list|(
name|parsedContent
operator|.
name|length
argument_list|()
argument_list|)
decl_stmt|;
name|additionalFields
operator|.
name|put
argument_list|(
name|Property
operator|.
name|CONTENT_LENGTH
operator|.
name|toLowerCase
argument_list|()
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"Error parsing document in field [{}]"
argument_list|,
name|e
argument_list|,
name|field
argument_list|)
throw|;
block|}
name|ingestDocument
operator|.
name|setFieldValue
argument_list|(
name|targetField
argument_list|,
name|additionalFields
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getType
specifier|public
name|String
name|getType
parameter_list|()
block|{
return|return
name|TYPE
return|;
block|}
DECL|method|getField
name|String
name|getField
parameter_list|()
block|{
return|return
name|field
return|;
block|}
DECL|method|getTargetField
name|String
name|getTargetField
parameter_list|()
block|{
return|return
name|targetField
return|;
block|}
DECL|method|getProperties
name|Set
argument_list|<
name|Property
argument_list|>
name|getProperties
parameter_list|()
block|{
return|return
name|properties
return|;
block|}
DECL|method|getIndexedChars
name|int
name|getIndexedChars
parameter_list|()
block|{
return|return
name|indexedChars
return|;
block|}
DECL|class|Factory
specifier|public
specifier|static
specifier|final
class|class
name|Factory
implements|implements
name|Processor
operator|.
name|Factory
block|{
DECL|field|DEFAULT_PROPERTIES
specifier|static
specifier|final
name|Set
argument_list|<
name|Property
argument_list|>
name|DEFAULT_PROPERTIES
init|=
name|EnumSet
operator|.
name|allOf
argument_list|(
name|Property
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Override
DECL|method|create
specifier|public
name|AttachmentProcessor
name|create
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Processor
operator|.
name|Factory
argument_list|>
name|registry
parameter_list|,
name|String
name|processorTag
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|config
parameter_list|)
throws|throws
name|Exception
block|{
name|String
name|field
init|=
name|readStringProperty
argument_list|(
name|TYPE
argument_list|,
name|processorTag
argument_list|,
name|config
argument_list|,
literal|"field"
argument_list|)
decl_stmt|;
name|String
name|targetField
init|=
name|readStringProperty
argument_list|(
name|TYPE
argument_list|,
name|processorTag
argument_list|,
name|config
argument_list|,
literal|"target_field"
argument_list|,
literal|"attachment"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|properyNames
init|=
name|readOptionalList
argument_list|(
name|TYPE
argument_list|,
name|processorTag
argument_list|,
name|config
argument_list|,
literal|"properties"
argument_list|)
decl_stmt|;
name|int
name|indexedChars
init|=
name|readIntProperty
argument_list|(
name|TYPE
argument_list|,
name|processorTag
argument_list|,
name|config
argument_list|,
literal|"indexed_chars"
argument_list|,
name|NUMBER_OF_CHARS_INDEXED
argument_list|)
decl_stmt|;
specifier|final
name|Set
argument_list|<
name|Property
argument_list|>
name|properties
decl_stmt|;
if|if
condition|(
name|properyNames
operator|!=
literal|null
condition|)
block|{
name|properties
operator|=
name|EnumSet
operator|.
name|noneOf
argument_list|(
name|Property
operator|.
name|class
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|fieldName
range|:
name|properyNames
control|)
block|{
try|try
block|{
name|properties
operator|.
name|add
argument_list|(
name|Property
operator|.
name|parse
argument_list|(
name|fieldName
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
throw|throw
name|newConfigurationException
argument_list|(
name|TYPE
argument_list|,
name|processorTag
argument_list|,
literal|"properties"
argument_list|,
literal|"illegal field option ["
operator|+
name|fieldName
operator|+
literal|"]. valid values are "
operator|+
name|Arrays
operator|.
name|toString
argument_list|(
name|Property
operator|.
name|values
argument_list|()
argument_list|)
argument_list|)
throw|;
block|}
block|}
block|}
else|else
block|{
name|properties
operator|=
name|DEFAULT_PROPERTIES
expr_stmt|;
block|}
return|return
operator|new
name|AttachmentProcessor
argument_list|(
name|processorTag
argument_list|,
name|field
argument_list|,
name|targetField
argument_list|,
name|properties
argument_list|,
name|indexedChars
argument_list|)
return|;
block|}
block|}
DECL|enum|Property
enum|enum
name|Property
block|{
DECL|enum constant|CONTENT
name|CONTENT
block|,
DECL|enum constant|TITLE
name|TITLE
block|,
DECL|enum constant|AUTHOR
name|AUTHOR
block|,
DECL|enum constant|KEYWORDS
name|KEYWORDS
block|,
DECL|enum constant|DATE
name|DATE
block|,
DECL|enum constant|CONTENT_TYPE
name|CONTENT_TYPE
block|,
DECL|enum constant|CONTENT_LENGTH
name|CONTENT_LENGTH
block|,
DECL|enum constant|LANGUAGE
name|LANGUAGE
block|;
DECL|method|parse
specifier|public
specifier|static
name|Property
name|parse
parameter_list|(
name|String
name|value
parameter_list|)
block|{
return|return
name|valueOf
argument_list|(
name|value
operator|.
name|toUpperCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
argument_list|)
return|;
block|}
DECL|method|toLowerCase
specifier|public
name|String
name|toLowerCase
parameter_list|()
block|{
return|return
name|this
operator|.
name|toString
argument_list|()
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

