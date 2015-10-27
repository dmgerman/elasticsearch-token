begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.ingest.processor.date
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|processor
operator|.
name|date
package|;
end_package

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
name|DateTimeFormat
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
name|DateTimeFormatter
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

begin_class
DECL|class|JodaPatternDateParser
specifier|public
class|class
name|JodaPatternDateParser
implements|implements
name|DateParser
block|{
DECL|field|formatter
specifier|private
specifier|final
name|DateTimeFormatter
name|formatter
decl_stmt|;
DECL|method|JodaPatternDateParser
specifier|public
name|JodaPatternDateParser
parameter_list|(
name|String
name|format
parameter_list|)
block|{
name|formatter
operator|=
name|DateTimeFormat
operator|.
name|forPattern
argument_list|(
name|format
argument_list|)
operator|.
name|withDefaultYear
argument_list|(
operator|(
operator|new
name|DateTime
argument_list|(
name|DateTimeZone
operator|.
name|UTC
argument_list|)
operator|)
operator|.
name|getYear
argument_list|()
argument_list|)
operator|.
name|withOffsetParsed
argument_list|()
expr_stmt|;
block|}
DECL|method|JodaPatternDateParser
specifier|public
name|JodaPatternDateParser
parameter_list|(
name|String
name|format
parameter_list|,
name|DateTimeZone
name|timezone
parameter_list|)
block|{
name|formatter
operator|=
name|DateTimeFormat
operator|.
name|forPattern
argument_list|(
name|format
argument_list|)
operator|.
name|withDefaultYear
argument_list|(
operator|(
operator|new
name|DateTime
argument_list|(
name|timezone
argument_list|)
operator|)
operator|.
name|getYear
argument_list|()
argument_list|)
operator|.
name|withZone
argument_list|(
name|timezone
argument_list|)
expr_stmt|;
block|}
DECL|method|JodaPatternDateParser
specifier|public
name|JodaPatternDateParser
parameter_list|(
name|String
name|format
parameter_list|,
name|Locale
name|locale
parameter_list|)
block|{
name|formatter
operator|=
name|DateTimeFormat
operator|.
name|forPattern
argument_list|(
name|format
argument_list|)
operator|.
name|withDefaultYear
argument_list|(
operator|(
operator|new
name|DateTime
argument_list|(
name|DateTimeZone
operator|.
name|UTC
argument_list|)
operator|)
operator|.
name|getYear
argument_list|()
argument_list|)
operator|.
name|withLocale
argument_list|(
name|locale
argument_list|)
expr_stmt|;
block|}
DECL|method|JodaPatternDateParser
specifier|public
name|JodaPatternDateParser
parameter_list|(
name|String
name|format
parameter_list|,
name|DateTimeZone
name|timezone
parameter_list|,
name|Locale
name|locale
parameter_list|)
block|{
name|formatter
operator|=
name|DateTimeFormat
operator|.
name|forPattern
argument_list|(
name|format
argument_list|)
operator|.
name|withDefaultYear
argument_list|(
operator|(
operator|new
name|DateTime
argument_list|(
name|DateTimeZone
operator|.
name|UTC
argument_list|)
operator|)
operator|.
name|getYear
argument_list|()
argument_list|)
operator|.
name|withZone
argument_list|(
name|timezone
argument_list|)
operator|.
name|withLocale
argument_list|(
name|locale
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|parseMillis
specifier|public
name|long
name|parseMillis
parameter_list|(
name|String
name|date
parameter_list|)
block|{
return|return
name|formatter
operator|.
name|parseMillis
argument_list|(
name|date
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|parseDateTime
specifier|public
name|DateTime
name|parseDateTime
parameter_list|(
name|String
name|date
parameter_list|)
block|{
return|return
name|formatter
operator|.
name|parseDateTime
argument_list|(
name|date
argument_list|)
return|;
block|}
block|}
end_class

end_unit

