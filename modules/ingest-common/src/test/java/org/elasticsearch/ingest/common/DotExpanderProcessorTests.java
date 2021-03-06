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
name|Map
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
name|equalTo
import|;
end_import

begin_class
DECL|class|DotExpanderProcessorTests
specifier|public
class|class
name|DotExpanderProcessorTests
extends|extends
name|ESTestCase
block|{
DECL|method|testEscapeFields
specifier|public
name|void
name|testEscapeFields
parameter_list|()
throws|throws
name|Exception
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|source
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|source
operator|.
name|put
argument_list|(
literal|"foo.bar"
argument_list|,
literal|"baz1"
argument_list|)
expr_stmt|;
name|IngestDocument
name|document
init|=
operator|new
name|IngestDocument
argument_list|(
name|source
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
decl_stmt|;
name|DotExpanderProcessor
name|processor
init|=
operator|new
name|DotExpanderProcessor
argument_list|(
literal|"_tag"
argument_list|,
literal|null
argument_list|,
literal|"foo.bar"
argument_list|)
decl_stmt|;
name|processor
operator|.
name|execute
argument_list|(
name|document
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|document
operator|.
name|getFieldValue
argument_list|(
literal|"foo"
argument_list|,
name|Map
operator|.
name|class
argument_list|)
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|document
operator|.
name|getFieldValue
argument_list|(
literal|"foo.bar"
argument_list|,
name|String
operator|.
name|class
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"baz1"
argument_list|)
argument_list|)
expr_stmt|;
name|source
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|source
operator|.
name|put
argument_list|(
literal|"foo.bar.baz"
argument_list|,
literal|"value"
argument_list|)
expr_stmt|;
name|document
operator|=
operator|new
name|IngestDocument
argument_list|(
name|source
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
expr_stmt|;
name|processor
operator|=
operator|new
name|DotExpanderProcessor
argument_list|(
literal|"_tag"
argument_list|,
literal|null
argument_list|,
literal|"foo.bar.baz"
argument_list|)
expr_stmt|;
name|processor
operator|.
name|execute
argument_list|(
name|document
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|document
operator|.
name|getFieldValue
argument_list|(
literal|"foo"
argument_list|,
name|Map
operator|.
name|class
argument_list|)
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|document
operator|.
name|getFieldValue
argument_list|(
literal|"foo.bar"
argument_list|,
name|Map
operator|.
name|class
argument_list|)
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|document
operator|.
name|getFieldValue
argument_list|(
literal|"foo.bar.baz"
argument_list|,
name|String
operator|.
name|class
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"value"
argument_list|)
argument_list|)
expr_stmt|;
name|source
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|source
operator|.
name|put
argument_list|(
literal|"foo.bar"
argument_list|,
literal|"baz1"
argument_list|)
expr_stmt|;
name|source
operator|.
name|put
argument_list|(
literal|"foo"
argument_list|,
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"bar"
argument_list|,
literal|"baz2"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|document
operator|=
operator|new
name|IngestDocument
argument_list|(
name|source
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
expr_stmt|;
name|processor
operator|=
operator|new
name|DotExpanderProcessor
argument_list|(
literal|"_tag"
argument_list|,
literal|null
argument_list|,
literal|"foo.bar"
argument_list|)
expr_stmt|;
name|processor
operator|.
name|execute
argument_list|(
name|document
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|document
operator|.
name|getSourceAndMetadata
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|document
operator|.
name|getFieldValue
argument_list|(
literal|"foo.bar"
argument_list|,
name|List
operator|.
name|class
argument_list|)
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|document
operator|.
name|getFieldValue
argument_list|(
literal|"foo.bar.0"
argument_list|,
name|String
operator|.
name|class
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"baz2"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|document
operator|.
name|getFieldValue
argument_list|(
literal|"foo.bar.1"
argument_list|,
name|String
operator|.
name|class
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"baz1"
argument_list|)
argument_list|)
expr_stmt|;
name|source
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|source
operator|.
name|put
argument_list|(
literal|"foo.bar"
argument_list|,
literal|"2"
argument_list|)
expr_stmt|;
name|source
operator|.
name|put
argument_list|(
literal|"foo"
argument_list|,
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"bar"
argument_list|,
literal|1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|document
operator|=
operator|new
name|IngestDocument
argument_list|(
name|source
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
expr_stmt|;
name|processor
operator|=
operator|new
name|DotExpanderProcessor
argument_list|(
literal|"_tag"
argument_list|,
literal|null
argument_list|,
literal|"foo.bar"
argument_list|)
expr_stmt|;
name|processor
operator|.
name|execute
argument_list|(
name|document
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|document
operator|.
name|getSourceAndMetadata
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|document
operator|.
name|getFieldValue
argument_list|(
literal|"foo.bar"
argument_list|,
name|List
operator|.
name|class
argument_list|)
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|document
operator|.
name|getFieldValue
argument_list|(
literal|"foo.bar.0"
argument_list|,
name|Integer
operator|.
name|class
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|document
operator|.
name|getFieldValue
argument_list|(
literal|"foo.bar.1"
argument_list|,
name|String
operator|.
name|class
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"2"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testEscapeFields_valueField
specifier|public
name|void
name|testEscapeFields_valueField
parameter_list|()
throws|throws
name|Exception
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|source
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|source
operator|.
name|put
argument_list|(
literal|"foo.bar"
argument_list|,
literal|"baz1"
argument_list|)
expr_stmt|;
name|source
operator|.
name|put
argument_list|(
literal|"foo"
argument_list|,
literal|"baz2"
argument_list|)
expr_stmt|;
name|IngestDocument
name|document1
init|=
operator|new
name|IngestDocument
argument_list|(
name|source
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
decl_stmt|;
name|Processor
name|processor1
init|=
operator|new
name|DotExpanderProcessor
argument_list|(
literal|"_tag"
argument_list|,
literal|null
argument_list|,
literal|"foo.bar"
argument_list|)
decl_stmt|;
comment|// foo already exists and if a leaf field and therefor can't be replaced by a map field:
name|Exception
name|e
init|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|processor1
operator|.
name|execute
argument_list|(
name|document1
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"cannot expend [foo.bar], because [foo] is not an object field, but a value field"
argument_list|)
argument_list|)
expr_stmt|;
comment|// so because foo is no branch field but a value field the `foo.bar` field can't be expanded
comment|// into [foo].[bar], so foo should be renamed first into `[foo].[bar]:
name|IngestDocument
name|document
init|=
operator|new
name|IngestDocument
argument_list|(
name|source
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
decl_stmt|;
name|Processor
name|processor
init|=
operator|new
name|RenameProcessor
argument_list|(
literal|"_tag"
argument_list|,
literal|"foo"
argument_list|,
literal|"foo.bar"
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|processor
operator|.
name|execute
argument_list|(
name|document
argument_list|)
expr_stmt|;
name|processor
operator|=
operator|new
name|DotExpanderProcessor
argument_list|(
literal|"_tag"
argument_list|,
literal|null
argument_list|,
literal|"foo.bar"
argument_list|)
expr_stmt|;
name|processor
operator|.
name|execute
argument_list|(
name|document
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|document
operator|.
name|getFieldValue
argument_list|(
literal|"foo"
argument_list|,
name|Map
operator|.
name|class
argument_list|)
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|document
operator|.
name|getFieldValue
argument_list|(
literal|"foo.bar.0"
argument_list|,
name|String
operator|.
name|class
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"baz2"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|document
operator|.
name|getFieldValue
argument_list|(
literal|"foo.bar.1"
argument_list|,
name|String
operator|.
name|class
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"baz1"
argument_list|)
argument_list|)
expr_stmt|;
name|source
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|source
operator|.
name|put
argument_list|(
literal|"foo.bar"
argument_list|,
literal|"baz1"
argument_list|)
expr_stmt|;
name|document
operator|=
operator|new
name|IngestDocument
argument_list|(
name|source
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
expr_stmt|;
name|processor
operator|=
operator|new
name|DotExpanderProcessor
argument_list|(
literal|"_tag"
argument_list|,
literal|null
argument_list|,
literal|"foo.bar"
argument_list|)
expr_stmt|;
name|processor
operator|.
name|execute
argument_list|(
name|document
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|document
operator|.
name|getFieldValue
argument_list|(
literal|"foo"
argument_list|,
name|Map
operator|.
name|class
argument_list|)
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|document
operator|.
name|getFieldValue
argument_list|(
literal|"foo.bar"
argument_list|,
name|String
operator|.
name|class
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"baz1"
argument_list|)
argument_list|)
expr_stmt|;
name|source
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|source
operator|.
name|put
argument_list|(
literal|"foo.bar.baz"
argument_list|,
literal|"baz1"
argument_list|)
expr_stmt|;
name|source
operator|.
name|put
argument_list|(
literal|"foo"
argument_list|,
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"bar"
argument_list|,
operator|new
name|HashMap
argument_list|<>
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|document
operator|=
operator|new
name|IngestDocument
argument_list|(
name|source
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
expr_stmt|;
name|processor
operator|=
operator|new
name|DotExpanderProcessor
argument_list|(
literal|"_tag"
argument_list|,
literal|null
argument_list|,
literal|"foo.bar.baz"
argument_list|)
expr_stmt|;
name|processor
operator|.
name|execute
argument_list|(
name|document
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|document
operator|.
name|getFieldValue
argument_list|(
literal|"foo"
argument_list|,
name|Map
operator|.
name|class
argument_list|)
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|document
operator|.
name|getFieldValue
argument_list|(
literal|"foo.bar"
argument_list|,
name|Map
operator|.
name|class
argument_list|)
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|document
operator|.
name|getFieldValue
argument_list|(
literal|"foo.bar.baz"
argument_list|,
name|String
operator|.
name|class
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"baz1"
argument_list|)
argument_list|)
expr_stmt|;
name|source
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|source
operator|.
name|put
argument_list|(
literal|"foo.bar.baz"
argument_list|,
literal|"baz1"
argument_list|)
expr_stmt|;
name|source
operator|.
name|put
argument_list|(
literal|"foo"
argument_list|,
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"bar"
argument_list|,
literal|"baz2"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|IngestDocument
name|document2
init|=
operator|new
name|IngestDocument
argument_list|(
name|source
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
decl_stmt|;
name|Processor
name|processor2
init|=
operator|new
name|DotExpanderProcessor
argument_list|(
literal|"_tag"
argument_list|,
literal|null
argument_list|,
literal|"foo.bar.baz"
argument_list|)
decl_stmt|;
name|e
operator|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|processor2
operator|.
name|execute
argument_list|(
name|document2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"cannot expend [foo.bar.baz], because [foo.bar] is not an object field, but a value field"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testEscapeFields_path
specifier|public
name|void
name|testEscapeFields_path
parameter_list|()
throws|throws
name|Exception
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|source
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|source
operator|.
name|put
argument_list|(
literal|"foo"
argument_list|,
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"bar.baz"
argument_list|,
literal|"value"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|IngestDocument
name|document
init|=
operator|new
name|IngestDocument
argument_list|(
name|source
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
decl_stmt|;
name|DotExpanderProcessor
name|processor
init|=
operator|new
name|DotExpanderProcessor
argument_list|(
literal|"_tag"
argument_list|,
literal|"foo"
argument_list|,
literal|"bar.baz"
argument_list|)
decl_stmt|;
name|processor
operator|.
name|execute
argument_list|(
name|document
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|document
operator|.
name|getFieldValue
argument_list|(
literal|"foo"
argument_list|,
name|Map
operator|.
name|class
argument_list|)
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|document
operator|.
name|getFieldValue
argument_list|(
literal|"foo.bar"
argument_list|,
name|Map
operator|.
name|class
argument_list|)
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|document
operator|.
name|getFieldValue
argument_list|(
literal|"foo.bar.baz"
argument_list|,
name|String
operator|.
name|class
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"value"
argument_list|)
argument_list|)
expr_stmt|;
name|source
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|source
operator|.
name|put
argument_list|(
literal|"field"
argument_list|,
operator|new
name|HashMap
argument_list|<>
argument_list|(
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"foo.bar.baz"
argument_list|,
literal|"value"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|document
operator|=
operator|new
name|IngestDocument
argument_list|(
name|source
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
expr_stmt|;
name|processor
operator|=
operator|new
name|DotExpanderProcessor
argument_list|(
literal|"_tag"
argument_list|,
literal|"field"
argument_list|,
literal|"foo.bar.baz"
argument_list|)
expr_stmt|;
name|processor
operator|.
name|execute
argument_list|(
name|document
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|document
operator|.
name|getFieldValue
argument_list|(
literal|"field.foo"
argument_list|,
name|Map
operator|.
name|class
argument_list|)
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|document
operator|.
name|getFieldValue
argument_list|(
literal|"field.foo.bar"
argument_list|,
name|Map
operator|.
name|class
argument_list|)
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|document
operator|.
name|getFieldValue
argument_list|(
literal|"field.foo.bar.baz"
argument_list|,
name|String
operator|.
name|class
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"value"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

