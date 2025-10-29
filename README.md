**AI-Powered Q&A System with Spring Boot, Ollama, and Weaviate**

**Application Design and Flow**
The project uses Spring Boot and Spring AI to build a REST API for answering questions based on company documents. 
A directory watcher monitors for new HTML files, which are manually extracted and placed in a folder. 
These files are chunked based on <h2> and <h3> tags, embedded using Ollama nomic-embed-text, and stored in Weaviate. 
The query-response flow begins when a user sends a question to the Spring Boot REST API. 
The query is embedded using Ollama nomic-embed-text and compared against stored vectors in Weaviate. 
The top relevant chunks are retrieved and combined into a context string. 
This context, along with the original query, is passed to Ollama Mistral, which generates a response.

![img.png](img.png)

**Technology Stack**
Spring Boot & Spring Web for application core and REST API.
Spring AI to integrate with Ollama models for embeddings and LLM responses.
Weaviate for vector storage and retrieval.
Jsoup for HTML parsing and chunking.
