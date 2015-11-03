begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.indexing
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|indexing
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
operator|.
name|Store
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
name|IntField
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
name|StringField
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
name|bytes
operator|.
name|BytesReference
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
name|json
operator|.
name|JsonXContent
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
name|indexing
operator|.
name|IndexingSlowLog
operator|.
name|SlowLogParsedDocumentPrinter
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
name|ParsedDocument
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|ESTestCase
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
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|containsString
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|not
import|;
end_import

begin_class
DECL|class|IndexingSlowLogTests
specifier|public
class|class
name|IndexingSlowLogTests
extends|extends
name|ESTestCase
block|{
DECL|method|testSlowLogParsedDocumentPrinterSourceToLog
specifier|public
name|void
name|testSlowLogParsedDocumentPrinterSourceToLog
parameter_list|()
throws|throws
name|IOException
block|{
name|BytesReference
name|source
init|=
name|JsonXContent
operator|.
name|contentBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"foo"
argument_list|,
literal|"bar"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|bytes
argument_list|()
decl_stmt|;
name|ParsedDocument
name|pd
init|=
operator|new
name|ParsedDocument
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"uid"
argument_list|,
literal|"test:id"
argument_list|,
name|Store
operator|.
name|YES
argument_list|)
argument_list|,
operator|new
name|IntField
argument_list|(
literal|"version"
argument_list|,
literal|1
argument_list|,
name|Store
operator|.
name|YES
argument_list|)
argument_list|,
operator|new
name|IntField
argument_list|(
literal|"seqNo"
argument_list|,
literal|1
argument_list|,
name|Store
operator|.
name|YES
argument_list|)
argument_list|,
literal|"id"
argument_list|,
literal|"test"
argument_list|,
literal|null
argument_list|,
literal|0
argument_list|,
operator|-
literal|1
argument_list|,
literal|null
argument_list|,
name|source
argument_list|,
literal|null
argument_list|)
decl_stmt|;
comment|// Turning off document logging doesn't log source[]
name|SlowLogParsedDocumentPrinter
name|p
init|=
operator|new
name|SlowLogParsedDocumentPrinter
argument_list|(
name|pd
argument_list|,
literal|10
argument_list|,
literal|true
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|p
operator|.
name|toString
argument_list|()
argument_list|,
name|not
argument_list|(
name|containsString
argument_list|(
literal|"source["
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// Turning on document logging logs the whole thing
name|p
operator|=
operator|new
name|SlowLogParsedDocumentPrinter
argument_list|(
name|pd
argument_list|,
literal|10
argument_list|,
literal|true
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|p
operator|.
name|toString
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"source[{\"foo\":\"bar\"}]"
argument_list|)
argument_list|)
expr_stmt|;
comment|// And you can truncate the source
name|p
operator|=
operator|new
name|SlowLogParsedDocumentPrinter
argument_list|(
name|pd
argument_list|,
literal|10
argument_list|,
literal|true
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|p
operator|.
name|toString
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"source[{\"f]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

