# ── Project layout ─────────────────────────────────────────────
#   ./backend/src     Java source (plain javac, no build tool)
#   ./frontend         Vite + React app (index.html, package.json, src/)
# ─────────────────────────────────────────────────────────────────

JAVA := java
JAVAC := javac
BACKEND_SRC   := backend/src/*.java
BACKEND_BIN := backend/bin
BACKEND_LIB := backend/lib/h2-2.4.240.jar
CLASSPATH := $(BACKEND_BIN):$(BACKEND_LIB)

# EDIT ME: fully-qualified name of the class with `public static void main`
MAIN_CLASS := backend.src.APIServer

.PHONY: help install build build-backend build-frontend \
        run-backend run-frontend dev clean

help:
	@echo "Available targets:"
	@echo "  make install        - install frontend dependencies"
	@echo "  make build          - compile backend + build frontend"
	@echo "  make build-backend  - javac-compile all backend .java files"
	@echo "  make build-frontend - npm run build (frontend)"
	@echo "  make run-backend    - run the compiled backend"
	@echo "  make run-frontend   - run the frontend dev server"
	@echo "  make dev            - run backend + frontend together"
	@echo "  make clean          - remove build output and node_modules"
	
install:
	cd frontend && npm install

# Compile every .java file under backend/src into backend/build,
# preserving package directory structure.
build-backend: 
	mkdir -p $(BACKEND_BIN)
	$(JAVAC) -d $(BACKEND_BIN) -cp $(BACKEND_LIB) $(BACKEND_SRC)

build-frontend:
	cd frontend && npm run build

build: build-backend build-frontend

run-backend: build-backend
	$(JAVA) -cp $(CLASSPATH) $(MAIN_CLASS) $(ARGS)

run-frontend:
	cd frontend && npm run dev

# Run backend and frontend together; Ctrl+C stops both.
dev: build-backend
	@trap 'kill 0' EXIT; \
	$(JAVA) -cp $(CLASSPATH) $(MAIN_CLASS) $(ARGS) & \
	cd frontend && npm run dev

clean:
	rm -rf $(BACKEND_BIN) frontend/node_modules frontend/dist