begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.ingest.common
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|common
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ExceptionsHelper
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
name|ConfigurationUtils
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
name|org
operator|.
name|joda
operator|.
name|time
operator|.
name|DateTime
import|;
end_import

begin_import
import|import
name|org
operator|.
name|joda
operator|.
name|time
operator|.
name|DateTimeZone
import|;
end_import

begin_import
import|import
name|org
operator|.
name|joda
operator|.
name|time
operator|.
name|format
operator|.
name|ISODateTimeFormat
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|IllformedLocaleException
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
name|function
operator|.
name|Function
import|;
end_import

begin_class
DECL|class|DateProcessor
specifier|public
specifier|final
class|class
name|DateProcessor
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
literal|"date"
decl_stmt|;
DECL|field|DEFAULT_TARGET_FIELD
specifier|static
specifier|final
name|String
name|DEFAULT_TARGET_FIELD
init|=
literal|"@timestamp"
decl_stmt|;
DECL|field|timezone
specifier|private
specifier|final
name|DateTimeZone
name|timezone
decl_stmt|;
DECL|field|locale
specifier|private
specifier|final
name|Locale
name|locale
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
DECL|field|formats
specifier|private
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|formats
decl_stmt|;
DECL|field|dateParsers
specifier|private
specifier|final
name|List
argument_list|<
name|Function
argument_list|<
name|String
argument_list|,
name|DateTime
argument_list|>
argument_list|>
name|dateParsers
decl_stmt|;
DECL|method|DateProcessor
name|DateProcessor
parameter_list|(
name|String
name|tag
parameter_list|,
name|DateTimeZone
name|timezone
parameter_list|,
name|Locale
name|locale
parameter_list|,
name|String
name|field
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|formats
parameter_list|,
name|String
name|targetField
parameter_list|)
block|{
name|super
argument_list|(
name|tag
argument_list|)
expr_stmt|;
name|this
operator|.
name|timezone
operator|=
name|timezone
expr_stmt|;
name|this
operator|.
name|locale
operator|=
name|locale
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
name|formats
operator|=
name|formats
expr_stmt|;
name|this
operator|.
name|dateParsers
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|this
operator|.
name|formats
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|format
range|:
name|formats
control|)
block|{
name|DateFormat
name|dateFormat
init|=
name|DateFormat
operator|.
name|fromString
argument_list|(
name|format
argument_list|)
decl_stmt|;
name|dateParsers
operator|.
name|add
argument_list|(
name|dateFormat
operator|.
name|getFunction
argument_list|(
name|format
argument_list|,
name|timezone
argument_list|,
name|locale
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
name|String
name|value
init|=
name|ingestDocument
operator|.
name|getFieldValue
argument_list|(
name|field
argument_list|,
name|String
operator|.
name|class
argument_list|)
decl_stmt|;
name|DateTime
name|dateTime
init|=
literal|null
decl_stmt|;
name|Exception
name|lastException
init|=
literal|null
decl_stmt|;
for|for
control|(
name|Function
argument_list|<
name|String
argument_list|,
name|DateTime
argument_list|>
name|dateParser
range|:
name|dateParsers
control|)
block|{
try|try
block|{
name|dateTime
operator|=
name|dateParser
operator|.
name|apply
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|//try the next parser and keep track of the exceptions
name|lastException
operator|=
name|ExceptionsHelper
operator|.
name|useOrSuppress
argument_list|(
name|lastException
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|dateTime
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"unable to parse date ["
operator|+
name|value
operator|+
literal|"]"
argument_list|,
name|lastException
argument_list|)
throw|;
block|}
name|ingestDocument
operator|.
name|setFieldValue
argument_list|(
name|targetField
argument_list|,
name|ISODateTimeFormat
operator|.
name|dateTime
argument_list|()
operator|.
name|print
argument_list|(
name|dateTime
argument_list|)
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
DECL|method|getTimezone
name|DateTimeZone
name|getTimezone
parameter_list|()
block|{
return|return
name|timezone
return|;
block|}
DECL|method|getLocale
name|Locale
name|getLocale
parameter_list|()
block|{
return|return
name|locale
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
DECL|method|getFormats
name|List
argument_list|<
name|String
argument_list|>
name|getFormats
parameter_list|()
block|{
return|return
name|formats
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
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|create
specifier|public
name|DateProcessor
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
name|ConfigurationUtils
operator|.
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
name|ConfigurationUtils
operator|.
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
name|DEFAULT_TARGET_FIELD
argument_list|)
decl_stmt|;
name|String
name|timezoneString
init|=
name|ConfigurationUtils
operator|.
name|readOptionalStringProperty
argument_list|(
name|TYPE
argument_list|,
name|processorTag
argument_list|,
name|config
argument_list|,
literal|"timezone"
argument_list|)
decl_stmt|;
name|DateTimeZone
name|timezone
init|=
name|timezoneString
operator|==
literal|null
condition|?
name|DateTimeZone
operator|.
name|UTC
else|:
name|DateTimeZone
operator|.
name|forID
argument_list|(
name|timezoneString
argument_list|)
decl_stmt|;
name|String
name|localeString
init|=
name|ConfigurationUtils
operator|.
name|readOptionalStringProperty
argument_list|(
name|TYPE
argument_list|,
name|processorTag
argument_list|,
name|config
argument_list|,
literal|"locale"
argument_list|)
decl_stmt|;
name|Locale
name|locale
init|=
name|Locale
operator|.
name|ENGLISH
decl_stmt|;
if|if
condition|(
name|localeString
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|locale
operator|=
operator|(
operator|new
name|Locale
operator|.
name|Builder
argument_list|()
operator|)
operator|.
name|setLanguageTag
argument_list|(
name|localeString
argument_list|)
operator|.
name|build
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllformedLocaleException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Invalid language tag specified: "
operator|+
name|localeString
argument_list|)
throw|;
block|}
block|}
name|List
argument_list|<
name|String
argument_list|>
name|formats
init|=
name|ConfigurationUtils
operator|.
name|readList
argument_list|(
name|TYPE
argument_list|,
name|processorTag
argument_list|,
name|config
argument_list|,
literal|"formats"
argument_list|)
decl_stmt|;
return|return
operator|new
name|DateProcessor
argument_list|(
name|processorTag
argument_list|,
name|timezone
argument_list|,
name|locale
argument_list|,
name|field
argument_list|,
name|formats
argument_list|,
name|targetField
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

