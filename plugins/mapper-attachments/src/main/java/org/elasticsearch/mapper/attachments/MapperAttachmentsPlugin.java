begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.mapper.attachments
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|mapper
operator|.
name|attachments
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|Logger
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
name|DeprecationLogger
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
name|ESLoggerFactory
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
name|Setting
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
name|plugins
operator|.
name|MapperPlugin
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|plugins
operator|.
name|Plugin
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
name|Collections
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
DECL|class|MapperAttachmentsPlugin
specifier|public
class|class
name|MapperAttachmentsPlugin
extends|extends
name|Plugin
implements|implements
name|MapperPlugin
block|{
DECL|field|logger
specifier|private
specifier|static
name|Logger
name|logger
init|=
name|ESLoggerFactory
operator|.
name|getLogger
argument_list|(
literal|"mapper.attachment"
argument_list|)
decl_stmt|;
DECL|field|deprecationLogger
specifier|private
specifier|static
name|DeprecationLogger
name|deprecationLogger
init|=
operator|new
name|DeprecationLogger
argument_list|(
name|logger
argument_list|)
decl_stmt|;
annotation|@
name|Override
DECL|method|getSettings
specifier|public
name|List
argument_list|<
name|Setting
argument_list|<
name|?
argument_list|>
argument_list|>
name|getSettings
parameter_list|()
block|{
name|deprecationLogger
operator|.
name|deprecated
argument_list|(
literal|"[mapper-attachments] plugin has been deprecated and will be replaced by [ingest-attachment] plugin."
argument_list|)
expr_stmt|;
return|return
name|Arrays
operator|.
name|asList
argument_list|(
name|AttachmentMapper
operator|.
name|INDEX_ATTACHMENT_DETECT_LANGUAGE_SETTING
argument_list|,
name|AttachmentMapper
operator|.
name|INDEX_ATTACHMENT_IGNORE_ERRORS_SETTING
argument_list|,
name|AttachmentMapper
operator|.
name|INDEX_ATTACHMENT_INDEXED_CHARS_SETTING
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|getMappers
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Mapper
operator|.
name|TypeParser
argument_list|>
name|getMappers
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"attachment"
argument_list|,
operator|new
name|AttachmentMapper
operator|.
name|TypeParser
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

