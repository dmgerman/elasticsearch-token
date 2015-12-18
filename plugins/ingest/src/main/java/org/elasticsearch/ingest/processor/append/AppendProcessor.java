begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.ingest.processor.append
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|processor
operator|.
name|append
package|;
end_package

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
name|TemplateService
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
name|ValueSource
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
name|processor
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
name|processor
operator|.
name|Processor
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
comment|/**  * Processor that appends value or values to existing lists. If the field is not present a new list holding the  * provided values will be added. If the field is a scalar it will be converted to a single item list and the provided  * values will be added to the newly created list.  */
end_comment

begin_class
DECL|class|AppendProcessor
specifier|public
class|class
name|AppendProcessor
implements|implements
name|Processor
block|{
DECL|field|TYPE
specifier|public
specifier|static
specifier|final
name|String
name|TYPE
init|=
literal|"append"
decl_stmt|;
DECL|field|field
specifier|private
specifier|final
name|TemplateService
operator|.
name|Template
name|field
decl_stmt|;
DECL|field|value
specifier|private
specifier|final
name|ValueSource
name|value
decl_stmt|;
DECL|method|AppendProcessor
name|AppendProcessor
parameter_list|(
name|TemplateService
operator|.
name|Template
name|field
parameter_list|,
name|ValueSource
name|value
parameter_list|)
block|{
name|this
operator|.
name|field
operator|=
name|field
expr_stmt|;
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
block|}
DECL|method|getField
specifier|public
name|TemplateService
operator|.
name|Template
name|getField
parameter_list|()
block|{
return|return
name|field
return|;
block|}
DECL|method|getValue
specifier|public
name|ValueSource
name|getValue
parameter_list|()
block|{
return|return
name|value
return|;
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
throws|throws
name|Exception
block|{
name|ingestDocument
operator|.
name|appendFieldValue
argument_list|(
name|field
argument_list|,
name|value
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
argument_list|<
name|AppendProcessor
argument_list|>
block|{
DECL|field|templateService
specifier|private
specifier|final
name|TemplateService
name|templateService
decl_stmt|;
DECL|method|Factory
specifier|public
name|Factory
parameter_list|(
name|TemplateService
name|templateService
parameter_list|)
block|{
name|this
operator|.
name|templateService
operator|=
name|templateService
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|create
specifier|public
name|AppendProcessor
name|create
parameter_list|(
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
name|config
argument_list|,
literal|"field"
argument_list|)
decl_stmt|;
name|Object
name|value
init|=
name|ConfigurationUtils
operator|.
name|readObject
argument_list|(
name|config
argument_list|,
literal|"value"
argument_list|)
decl_stmt|;
return|return
operator|new
name|AppendProcessor
argument_list|(
name|templateService
operator|.
name|compile
argument_list|(
name|field
argument_list|)
argument_list|,
name|ValueSource
operator|.
name|wrap
argument_list|(
name|value
argument_list|,
name|templateService
argument_list|)
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

